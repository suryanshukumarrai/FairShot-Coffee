package com.coffeeShop.Coffee.Shop.service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import com.coffeeShop.Coffee.Shop.model.Barista;
import com.coffeeShop.Coffee.Shop.model.Order;
import com.coffeeShop.Coffee.Shop.model.OrderStatus;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing order completion lifecycle using Spring TaskScheduler.
 * 
 * This service ensures orders are completed automatically after their
 * preparation time, freeing baristas for new work immediately.
 * 
 * Key improvements over raw ScheduledExecutorService:
 * - Spring manages lifecycle properly
 * - Tasks actually execute (no silent failures)
 * - Clean shutdown handling
 * - Thread pool management by Spring
 */
@Service
@Slf4j
public class OrderCompletionService {

    private final TaskScheduler taskScheduler;
    private final ComplaintService complaintService;
    private final com.coffeeShop.Coffee.Shop.service.BaristaAssignmentService baristaAssignmentService;
    private final ManagerMetricsService managerMetricsService;
    private final ConcurrentHashMap<String, ScheduledFuture<?>> scheduledCompletions;

    @Autowired
    public OrderCompletionService(TaskScheduler taskScheduler,
            ComplaintService complaintService,
            ManagerMetricsService managerMetricsService,
            @Lazy com.coffeeShop.Coffee.Shop.service.BaristaAssignmentService baristaAssignmentService) {
        this.taskScheduler = taskScheduler;
        this.complaintService = complaintService;
        this.managerMetricsService = managerMetricsService;
        this.baristaAssignmentService = baristaAssignmentService;
        this.scheduledCompletions = new ConcurrentHashMap<>();

        log.info("OrderCompletionService initialized with Spring TaskScheduler");
    }

    /**
     * Schedules completion of an order after its preparation time.
     * 
     * This method uses Spring's TaskScheduler which properly manages
     * task execution lifecycle, unlike raw ScheduledExecutorService.
     * 
     * @param order   The order to complete
     * @param barista The barista working on the order
     */
    public void scheduleOrderCompletion(Order order, Barista barista) {
        try {
            // Validate inputs
            if (order == null || barista == null) {
                log.warn("Invalid completion request: order={}, barista={}", order, barista);
                return;
            }

            // Check if order is already completed
            if (order.getStatus() == OrderStatus.COMPLETED) {
                log.debug("Order {} already completed, skipping completion", order.getId());
                return;
            }

            // Check if completion already scheduled
            String completionKey = order.getId().toString();
            if (scheduledCompletions.containsKey(completionKey)) {
                log.debug("Completion already scheduled for order {}", order.getId());
                return;
            }

            // Calculate completion time (NOW + prep time)
            int prepTimeMinutes = order.getPrepTimeMinutes();
            Instant completionTime = Instant.now().plusSeconds(prepTimeMinutes * 60L);

            // Schedule completion task using Spring TaskScheduler
            ScheduledFuture<?> completionTask = taskScheduler.schedule(() -> {
                try {
                    log.info("⏰ EXECUTING COMPLETION: Order {} scheduled for {}",
                            order.getId(), completionTime);
                    completeOrder(order, barista);
                } catch (Exception e) {
                    log.error("Error during scheduled completion for order {}: {}",
                            order.getId(), e.getMessage(), e);
                }
            }, completionTime);

            // Track the scheduled completion
            scheduledCompletions.put(completionKey, completionTask);

            log.info("✅ COMPLETION SCHEDULED: Order {} ({}) will complete at {} ({} min prep)",
                    order.getId(),
                    order.getDrinkType().getDisplayName(),
                    completionTime,
                    prepTimeMinutes);

        } catch (Exception e) {
            log.error("Error scheduling completion for order {}: {}",
                    order != null ? order.getId() : "null", e.getMessage(), e);
        }
    }

    /**
     * Completes an order and frees the associated barista.
     * 
     * This method performs the actual completion logic:
     * 1. Marks order as COMPLETED
     * 2. Sets completion timestamp
     * 3. Frees the barista for new work
     * 4. Auto-resolves complaints
     * 5. Cleans up scheduled completion tracking
     * 
     * This method is idempotent - safe to call multiple times.
     */
    private void completeOrder(Order order, Barista barista) {
        try {
            // Double-check order state (idempotency)
            if (order.getStatus() == OrderStatus.COMPLETED) {
                log.debug("Order {} already completed during completion execution", order.getId());
                return;
            }

            // Mark order as completed
            order.setStatus(OrderStatus.COMPLETED);
            order.setCompletionTime(Instant.now());

            // Free the barista atomically
            if (barista != null) {
                barista.completeOrder();
            }

            // Calculate and log total wait time
            long totalWaitMinutes = 0;
            if (order.getArrivalTime() != null) {
                totalWaitMinutes = Duration.between(order.getArrivalTime(), Instant.now()).toMinutes();
            }

            // 🔥 TRACK METRICS
            managerMetricsService.incrementCoffeesServed(order.getDrinkType());
            managerMetricsService.recordWaitTime(Duration.between(order.getArrivalTime(), Instant.now()));
            if (totalWaitMinutes >= 8) {
                managerMetricsService.incrementSlaViolations();
            }
            if (barista != null) {
                managerMetricsService.recordBaristaWork(barista.getId(), order.getPrepTimeMinutes());
            }

            log.info("✅ ORDER COMPLETED: Order {} ({}) completed by {} (total wait: {} min, prep time: {} min)",
                    order.getId(),
                    order.getDrinkType().getDisplayName(),
                    barista != null ? barista.getName() : "Unknown",
                    totalWaitMinutes,
                    order.getPrepTimeMinutes());

            // Auto-resolve any complaints for this order
            complaintService.autoResolveComplaintsForOrder(order.getId());

            // 🔥 CRITICAL FIX: Trigger immediate assignment check
            // This eliminates idle baristas and queue pileups
            baristaAssignmentService.tryAssignNextOrder();

            // Clean up scheduled completion tracking
            String completionKey = order.getId().toString();
            ScheduledFuture<?> scheduledTask = scheduledCompletions.remove(completionKey);
            if (scheduledTask != null && !scheduledTask.isDone()) {
                scheduledTask.cancel(false);
            }

            // Log barista availability
            if (barista != null) {
                log.info("🟢 BARISTA AVAILABLE: {} is now free for new orders", barista.getName());
            }

        } catch (Exception e) {
            log.error("Error completing order {}: {}", order.getId(), e.getMessage(), e);
        }
    }

    /**
     * Triggers immediate assignment check after an order completes.
     * This is called by BaristaAssignmentService after freeing a barista.
     * 
     * @param baristaAssignmentService the assignment service to trigger
     */
    public void triggerImmediateAssignment(
            com.coffeeShop.Coffee.Shop.service.BaristaAssignmentService baristaAssignmentService) {
        if (baristaAssignmentService != null) {
            baristaAssignmentService.tryAssignNextOrder();
        }
    }

    /**
     * Cancels a scheduled completion if the order was cancelled/reassigned.
     * 
     * @param orderId The order ID to cancel completion for
     */
    public void cancelScheduledCompletion(String orderId) {
        ScheduledFuture<?> scheduledTask = scheduledCompletions.remove(orderId);
        if (scheduledTask != null && !scheduledTask.isDone()) {
            scheduledTask.cancel(false);
            log.info("Cancelled scheduled completion for order {}", orderId);
        }
    }

    /**
     * Cancels ALL scheduled completions.
     * CRITICAL for simulation reset - prevents ghost completions.
     */
    public void cancelAll() {
        int cancelledCount = 0;
        for (ScheduledFuture<?> future : scheduledCompletions.values()) {
            if (future != null && !future.isDone()) {
                future.cancel(true);
                cancelledCount++;
            }
        }
        scheduledCompletions.clear();
        log.info("Cancelled {} scheduled completions", cancelledCount);
    }
}
