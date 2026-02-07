package com.coffeeShop.Coffee.Shop.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.coffeeShop.Coffee.Shop.model.Barista;
import com.coffeeShop.Coffee.Shop.model.Order;
import com.coffeeShop.Coffee.Shop.model.OrderStatus;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

/**
 * Manages barista assignment and workload balancing.
 * 
 * This service is responsible for:
 * 1. Initializing 3 baristas at startup
 * 2. Tracking barista availability and workload
 * 3. Automatically assigning orders when baristas become free
 * 4. Balancing workload across baristas
 * 5. Handling emergency orders (wait >= 8 minutes)
 * 
 * Workload Balancing Logic:
 * - workloadRatio = baristaWork / averageWork
 * - if ratio > 1.2 → prefer short prepTime (reduce workload)
 * - if ratio < 0.8 → allow long prepTime (increase workload)
 * 
 * This ensures efficient resource utilization and prevents burnout.
 */
@Service
@Slf4j
public class BaristaAssignmentService {

    private final QueueSchedulerService queueSchedulerService;
    private final PriorityScoreService priorityScoreService;
    private final FairnessTrackerService fairnessTrackerService;
    private final OrderCompletionService orderCompletionService;
    private final ExplanationService explanationService;

    // Thread-safe storage for baristas
    private ConcurrentHashMap<Integer, Barista> baristas;
    private final ReentrantReadWriteLock baristasLock;

    // Executor for automatic order assignment
    private final ScheduledExecutorService assignmentExecutor;

    // Constants for workload balancing
    private static final double HIGH_WORKLOAD_THRESHOLD = 1.2;
    private static final double LOW_WORKLOAD_THRESHOLD = 0.8;
    private static final int ASSIGNMENT_CHECK_INTERVAL_SECONDS = 2;

    @Autowired
    public BaristaAssignmentService(QueueSchedulerService queueSchedulerService,
            PriorityScoreService priorityScoreService,
            FairnessTrackerService fairnessTrackerService,
            OrderCompletionService orderCompletionService,
            ExplanationService explanationService) {
        this.queueSchedulerService = queueSchedulerService;
        this.priorityScoreService = priorityScoreService;
        this.fairnessTrackerService = fairnessTrackerService;
        this.orderCompletionService = orderCompletionService;
        this.explanationService = explanationService;
        this.baristas = new ConcurrentHashMap<>();
        this.baristasLock = new ReentrantReadWriteLock();
        this.assignmentExecutor = Executors.newScheduledThreadPool(1);
    }

    /**
     * Initializes 3 baristas for the coffee shop.
     * Creates baristas with different skill levels for realistic workload
     * distribution.
     */
    @PostConstruct
    public void initializeBaristas() {
        baristas = new ConcurrentHashMap<>();

        // Create 3 baristas with different characteristics
        Barista barista1 = Barista.builder()
                .id(1)
                .name("Barista 1")
                .busyUntil(null)
                .currentOrder(null)
                .totalWorkedMinutes(0)
                .build();

        Barista barista2 = Barista.builder()
                .id(2)
                .name("Barista 2")
                .busyUntil(null)
                .currentOrder(null)
                .totalWorkedMinutes(0)
                .build();

        Barista barista3 = Barista.builder()
                .id(3)
                .name("Barista 3")
                .busyUntil(null)
                .currentOrder(null)
                .totalWorkedMinutes(0)
                .build();

        baristas.put(1, barista1);
        baristas.put(2, barista2);
        baristas.put(3, barista3);

        log.info("INITIALIZED: 3 baristas ready for work");

        // Start automatic order assignment
        startAutomaticAssignment();

        log.info("Barista initialization completed. Ready to process orders.");
    }

    /**
     * Event-driven assignment: attempts to assign next order to any available
     * barista.
     * 
     * This is the PRIMARY assignment mechanism triggered immediately after
     * completions.
     * Simple, atomic, and guaranteed-safe.
     * 
     * Synchronized to prevent race conditions during parallel execution.
     */
    public synchronized void tryAssignNextOrder() {
        try {
            // Check each barista for availability
            for (Barista barista : baristas.values()) {
                if (!barista.isAvailable()) {
                    continue;
                }

                // Get next order from queue (atomic)
                Order order = queueSchedulerService.getNextOrder();
                if (order == null) {
                    // No more orders in queue
                    return;
                }

                // 🔥 CRITICAL: Update order status BEFORE assigning to barista
                // This ensures order.status == IN_PROGRESS is always true when barista.currentOrder != null
                order.setStatus(OrderStatus.IN_PROGRESS);
                order.setStartTime(Instant.now());
                order.setAssignedBaristaId(barista.getId());

                // Now assign to barista (maintains invariant)
                barista.assignOrder(order);

                // Schedule automatic completion
                orderCompletionService.scheduleOrderCompletion(order, barista);

                log.info("✅ ORDER ASSIGNED: Order {} ({}) → {} (prep: {} min)",
                        order.getId(),
                        order.getDrinkType().getDisplayName(),
                        barista.getName(),
                        order.getPrepTimeMinutes());
            }
        } catch (Exception e) {
            log.error("Error in tryAssignNextOrder: {}", e.getMessage(), e);
        }
    }

    /**
     * Starts automatic order assignment polling (BACKUP ONLY).
     * Primary assignment happens via tryAssignNextOrder() after completions.
     */
    private void startAutomaticAssignment() {
        assignmentExecutor.scheduleAtFixedRate(
                this::assignOrdersToAvailableBaristas,
                0,
                10, // Increased from 2 to 10 seconds - backup only
                TimeUnit.SECONDS);

        log.info("Started automatic order assignment (every 10 seconds - backup only)");
    }

    /**
     * Main assignment logic - checks for available baristas and assigns orders.
     * This is the core execution flow that keeps the system running.
     */
    private void assignOrdersToAvailableBaristas() {
        try {
            List<Barista> availableBaristas = getAvailableBaristas();

            if (availableBaristas.isEmpty()) {
                log.debug("No available baristas for order assignment");
                return;
            }

            int queueSize = queueSchedulerService.getQueueSize();
            if (queueSize == 0) {
                log.debug("No orders in shared queue to assign");
                return;
            }

            log.info("ASSIGNMENT CHECK: {} available baristas, {} orders in shared queue",
                    availableBaristas.size(), queueSize);

            // Assign orders to each available barista
            for (Barista barista : availableBaristas) {
                Order orderToAssign = selectNextOrder(barista);

                if (orderToAssign != null) {
                    assignOrderToBarista(orderToAssign, barista);
                } else {
                    log.debug("No suitable order found for {}", barista.getName());
                }
            }

        } catch (Exception e) {
            log.error("Error during automatic order assignment: {}", e.getMessage(), e);
        }
    }

    /**
     * Assigns an order to a barista and updates all relevant state.
     */
    private void assignOrderToBarista(Order order, Barista barista) {
        try {
            // Get all pending orders before assignment to track skips
            List<Order> pendingOrders = queueSchedulerService.getAllOrders();

            // CRITICAL: Get order from shared queue via QueueSchedulerService
            Order assignedOrder = queueSchedulerService.getNextOrder();
            if (assignedOrder == null) {
                log.debug("No orders available in shared queue for {}", barista.getName());
                return;
            }

            // Verify it's the expected order
            if (!assignedOrder.getId().equals(order.getId())) {
                log.debug("Order {} was already taken by another barista", order.getId());
                return;
            }

            // Track fairness: increment skip count for orders that arrived before this one
            for (Order pending : pendingOrders) {
                if (!pending.getId().equals(assignedOrder.getId()) &&
                        pending.getArrivalTime().isBefore(assignedOrder.getArrivalTime())) {
                    pending.incrementSkipCount();

                    // Generate explanation if skipped
                    if (pending.getSkipCount() == 1) {
                        pending.setExplanation(explanationService.generateSkipExplanation(1));
                    } else if (pending.getSkipCount() > 1) {
                        pending.setExplanation(explanationService.generateSkipExplanation(pending.getSkipCount()));
                    }

                    // Apply fairness penalty if needed
                    if (pending.shouldApplyFairnessPenalty()) {
                        pending.setExplanation(explanationService.generateFairnessExplanation());
                        log.warn("⚖️ FAIRNESS PENALTY: Order {} has been skipped {} times, boosting priority",
                                pending.getId(), pending.getSkipCount());
                    }
                }
            }

            // 🔥 AUTO-START: Set order status and start time IMMEDIATELY
            assignedOrder.setStatus(OrderStatus.IN_PROGRESS);
            assignedOrder.setStartTime(Instant.now());
            assignedOrder.setAssignedBaristaId(barista.getId());

            // Assign order to barista (this updates order state automatically)
            barista.assignOrder(assignedOrder);

            // Reset skip count for assigned order
            assignedOrder.setSkipCount(0);

            // Schedule automatic completion
            orderCompletionService.scheduleOrderCompletion(assignedOrder, barista);

            log.info("ORDER ASSIGNED: Order {} ({}) assigned to {} (prep time: {} min, queue size: {})",
                    assignedOrder.getId(),
                    assignedOrder.getDrinkType().getDisplayName(),
                    barista.getName(),
                    assignedOrder.getPrepTimeMinutes(),
                    queueSchedulerService.getQueueSize());

        } catch (Exception e) {
            log.error("Error assigning order {} to {}: {}",
                    order.getId(), barista.getName(), e.getMessage(), e);
        }
    }

    /**
     * Selects the next order for a barista considering workload balancing.
     * 
     * Workload Balancing Logic:
     * - Calculate workload ratio for the barista
     * - If ratio > 1.2 (high workload) → prefer short prep times
     * - If ratio < 0.8 (low workload) → allow long prep times
     * - Otherwise → use normal priority ordering
     */
    private Order selectNextOrder(Barista barista) {
        try {
            List<Order> allOrders = queueSchedulerService.getAllOrders();

            if (allOrders.isEmpty()) {
                return null;
            }

            // Check for emergency orders first (wait >= 8 minutes)
            Order emergencyOrder = findEmergencyOrder(allOrders);
            if (emergencyOrder != null) {
                log.info("EMERGENCY: Found order {} waiting {} minutes, assigning to {}",
                        emergencyOrder.getId(),
                        Duration.between(emergencyOrder.getArrivalTime(), Instant.now()).toMinutes(),
                        barista.getName());
                return emergencyOrder;
            }

            // Apply workload balancing
            double workloadRatio = calculateWorkloadRatio(barista);

            if (workloadRatio > HIGH_WORKLOAD_THRESHOLD) {
                // High workload - prefer short prep times
                return selectShortPrepTimeOrder(allOrders);
            } else if (workloadRatio < LOW_WORKLOAD_THRESHOLD) {
                // Low workload - allow long prep times
                return selectLongPrepTimeOrder(allOrders);
            } else {
                // Balanced workload - use normal priority
                return selectHighestPriorityOrder(allOrders);
            }

        } catch (Exception e) {
            log.error("Error selecting next order for {}: {}", barista.getName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * Finds an emergency order (wait time >= 8 minutes).
     * Emergency orders get immediate priority regardless of other factors.
     */
    private Order findEmergencyOrder(List<Order> orders) {
        Instant now = Instant.now();

        for (Order order : orders) {
            long waitMinutes = Duration.between(order.getArrivalTime(), now).toMinutes();
            if (waitMinutes >= 8) {
                return order;
            }
        }

        return null;
    }

    /**
     * Selects the order with the shortest preparation time.
     * Used when a barista has high workload to reduce their burden.
     */
    private Order selectShortPrepTimeOrder(List<Order> orders) {
        return orders.stream()
                .min((o1, o2) -> Integer.compare(o1.getPrepTimeMinutes(), o2.getPrepTimeMinutes()))
                .orElse(null);
    }

    /**
     * Selects the order with the longest preparation time.
     * Used when a barista has low workload to increase their utilization.
     */
    private Order selectLongPrepTimeOrder(List<Order> orders) {
        return orders.stream()
                .max((o1, o2) -> Integer.compare(o1.getPrepTimeMinutes(), o2.getPrepTimeMinutes()))
                .orElse(null);
    }

    /**
     * Selects the order with the highest priority score.
     * Used when workload is balanced.
     */
    private Order selectHighestPriorityOrder(List<Order> orders) {
        return orders.stream()
                .max((o1, o2) -> Double.compare(
                        priorityScoreService.calculatePriorityScore(o1),
                        priorityScoreService.calculatePriorityScore(o2)))
                .orElse(null);
    }

    /**
     * Gets list of available baristas.
     * A barista is available if they're not currently working on an order.
     */
    private List<Barista> getAvailableBaristas() {
        return baristas.values().stream()
                .filter(Barista::isAvailable)
                .toList();
    }

    /**
     * Gets all baristas (for monitoring).
     */
    public List<Barista> getAllBaristas() {
        try {
            baristasLock.readLock().lock();
            return new java.util.ArrayList<>(baristas.values());
        } finally {
            baristasLock.readLock().unlock();
        }
    }

    /**
     * Triggers immediate assignment check.
     * Called when a barista completes an order for faster response than polling.
     */
    public void triggerImmediateAssignment() {
        log.debug("Immediate assignment triggered");
        assignmentExecutor.submit(this::assignOrdersToAvailableBaristas);
    }

    /**
     * Calculates workload ratio for a barista.
     * workloadRatio = baristaWork / averageWork
     */
    private double calculateWorkloadRatio(Barista barista) {
        double averageWork = getAverageWorkload();
        double baristaWork = getBaristaWorkload(barista);

        if (averageWork == 0) {
            return 1.0; // No work done yet, treat as balanced
        }

        return baristaWork / averageWork;
    }

    /**
     * Gets the average workload across all baristas.
     */
    private double getAverageWorkload() {
        baristasLock.readLock().lock();
        try {
            return baristas.values().stream()
                    .mapToInt(this::getBaristaWorkload)
                    .average()
                    .orElse(0.0);
        } finally {
            baristasLock.readLock().unlock();
        }
    }

    /**
     * Gets the workload for a specific barista.
     * For now, we'll use a simple metric based on recent assignments.
     */
    private int getBaristaWorkload(Barista barista) {
        // Simple implementation: count of total worked minutes
        // In a real system, this could be more sophisticated
        Integer workedMinutes = barista.getTotalWorkedMinutes();
        return workedMinutes != null ? workedMinutes : 0;
    }

    /**
     * Resets all baristas to their initial state.
     * Used for simulation reset.
     */
    public void resetAllBaristas() {
        baristasLock.writeLock().lock();
        try {
            for (Barista barista : baristas.values()) {
                barista.forceReset();
            }
            log.info("Reset all {} baristas to initial state", baristas.size());
        } finally {
            baristasLock.writeLock().unlock();
        }
    }

    /**
     * Cleanup method called when the application shuts down.
     */
    @PreDestroy
    public void shutdown() {
        try {
            log.info("Shutting down BaristaAssignmentService...");

            // Shutdown assignment executor
            if (assignmentExecutor != null) {
                assignmentExecutor.shutdown();
                if (!assignmentExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    assignmentExecutor.shutdownNow();
                    log.warn("BaristaAssignmentService executor shutdown forced");
                }
            }

            // Spring TaskScheduler automatically manages shutdown via ApplicationContext
            // No manual shutdown needed for orderCompletionService

            log.info("BaristaAssignmentService shutdown completed");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("BaristaAssignmentService shutdown interrupted: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error during BaristaAssignmentService shutdown: {}", e.getMessage(), e);
        }
    }
}
