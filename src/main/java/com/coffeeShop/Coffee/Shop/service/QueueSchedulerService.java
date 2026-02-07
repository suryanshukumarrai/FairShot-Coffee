package com.coffeeShop.Coffee.Shop.service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.stereotype.Service;

import com.coffeeShop.Coffee.Shop.model.Order;

import lombok.extern.slf4j.Slf4j;

/**
 * Thread-safe queue scheduler that maintains a priority-based order queue.
 * 
 * This service is the core of the scheduling system and provides:
 * 1. PriorityBlockingQueue for thread-safe operations
 * 2. Custom comparator that considers priority scores and arrival times
 * 3. Dynamic re-prioritization as conditions change
 * 4. Fairness tracking integration
 * 
 * Why this beats FIFO:
 * - FIFO processes orders purely by arrival time, ignoring urgency and
 * efficiency
 * - This system balances multiple factors to minimize overall wait time
 * - Short jobs are prioritized (reduces queue length and average wait)
 * - Urgent orders get emergency processing (prevents SLA violations)
 * - Fairness is maintained through skip tracking and penalties
 */
@Service
@Slf4j
public class QueueSchedulerService {

    // CRITICAL FIX #3: Removed 'final' to allow atomic swap during reheapify
    private PriorityBlockingQueue<Order> orderQueue;
    private final PriorityScoreService priorityScoreService;
    private final FairnessTrackerService fairnessTrackerService;

    // Cache for priority scores to avoid recalculation on every comparison
    private final ConcurrentHashMap<java.util.UUID, Double> priorityScoreCache;

    // Read-write lock for cache operations
    private final ReentrantReadWriteLock cacheLock;

    public QueueSchedulerService(PriorityBlockingQueue<Order> orderQueue,
            PriorityScoreService priorityScoreService,
            FairnessTrackerService fairnessTrackerService) {
        this.orderQueue = orderQueue; // Use injected Spring bean, NOT new instance
        this.priorityScoreService = priorityScoreService;
        this.fairnessTrackerService = fairnessTrackerService;
        this.priorityScoreCache = new ConcurrentHashMap<>();
        this.cacheLock = new ReentrantReadWriteLock();

        log.info("QueueSchedulerService initialized with shared PriorityBlockingQueue");
    }

    /**
     * Adds a new order to the queue with thread safety.
     * Records the order for fairness tracking and initializes priority score.
     */
    public void addOrder(Order order) {
        // Record order for fairness tracking
        fairnessTrackerService.recordOrderAdded(order);

        // Calculate and cache initial priority score
        updatePriorityScore(order);

        // Add to queue (thread-safe operation)
        orderQueue.add(order);

        log.info("ORDER ADDED: Order {} ({}) added to queue. Queue size: {}",
                order.getId(),
                order.getDrinkType().getDisplayName(),
                orderQueue.size());
    }

    /**
     * Gets the next highest priority order from the queue.
     * Updates fairness tracking for all remaining orders.
     */
    public Order getNextOrder() {
        Order nextOrder = orderQueue.poll();

        if (nextOrder != null) {
            // Update fairness tracking for the processed order
            List<Order> remainingOrders = List.copyOf(orderQueue);
            fairnessTrackerService.recordOrderProcessedWithFairnessUpdate(nextOrder, remainingOrders);
            fairnessTrackerService.recordOrderProcessed(nextOrder);

            // Remove from cache
            cacheLock.writeLock().lock();
            try {
                priorityScoreCache.remove(nextOrder.getId());
            } finally {
                cacheLock.writeLock().unlock();
            }

            log.info("ORDER RETRIEVED: Order {} ({}) retrieved from queue. Queue size: {}",
                    nextOrder.getId(),
                    nextOrder.getDrinkType().getDisplayName(),
                    orderQueue.size());
        }

        return nextOrder;
    }

    /**
     * Rebuilds the priority queue with updated priority scores.
     * This is called periodically to ensure scores reflect current conditions.
     */
    public void reheapifyQueue() {
        log.debug("Rebuilding priority queue with updated scores...");

        // Get all current orders (immutable snapshot)
        List<Order> currentOrders = List.copyOf(orderQueue);

        // Build new queue with same comparator
        PriorityBlockingQueue<Order> newQueue = new PriorityBlockingQueue<>(100, orderQueue.comparator());

        // Clear cache
        cacheLock.writeLock().lock();
        try {
            priorityScoreCache.clear();

            // Re-add all orders with updated scores to new queue
            for (Order order : currentOrders) {
                double score = priorityScoreService.calculatePriorityScore(order);
                priorityScoreCache.put(order.getId(), score);
                newQueue.add(order);
            }
        } finally {
            cacheLock.writeLock().unlock();
        }

        // CRITICAL FIX #3: Atomic swap - prevents race conditions
        synchronized (this) {
            this.orderQueue = newQueue;
        }

        log.debug("Rebuilt priority queue with {} orders", newQueue.size());
    }

    /**
     * Updates the priority score for a specific order and caches it.
     */
    public void updatePriorityScore(Order order) {
        double score = priorityScoreService.calculatePriorityScore(order);

        cacheLock.writeLock().lock();
        try {
            priorityScoreCache.put(order.getId(), score);
        } finally {
            cacheLock.writeLock().unlock();
        }

        log.debug("Updated priority score for order {}: {}", order.getId(), score);
    }

    /**
     * Gets the cached priority score for an order.
     * Calculates and caches if not already present.
     */
    private double getCachedPriorityScore(Order order) {
        cacheLock.readLock().lock();
        try {
            Double cachedScore = priorityScoreCache.get(order.getId());
            if (cachedScore != null) {
                return cachedScore;
            }
        } finally {
            cacheLock.readLock().unlock();
        }

        // Calculate and cache if not found
        updatePriorityScore(order);
        cacheLock.readLock().lock();
        try {
            return priorityScoreCache.get(order.getId());
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    /**
     * Gets the current queue size.
     */
    public int getQueueSize() {
        return orderQueue.size();
    }

    /**
     * Clears all orders from the queue.
     * Used for simulation reset.
     */
    public void clearQueue() {
        int clearedCount = orderQueue.size();
        orderQueue.clear();
        
        // Clear cache
        cacheLock.writeLock().lock();
        try {
            priorityScoreCache.clear();
        } finally {
            cacheLock.writeLock().unlock();
        }
        
        log.info("Cleared {} orders from queue", clearedCount);
    }

    /**
     * Gets all orders currently in the queue.
     * Returns a copy to prevent concurrent modification.
     */
    public List<Order> getAllOrders() {
        return List.copyOf(orderQueue);
    }

    /**
     * Checks if the queue is empty.
     */
    public boolean isEmpty() {
        return orderQueue.isEmpty();
    }

    /**
     * Gets priority score for monitoring purposes.
     */
    public double getPriorityScore(Order order) {
        return getCachedPriorityScore(order);
    }

    /**
     * Applies emergency boost to orders approaching the 10-minute limit.
     * This is called by the scheduled task to ensure SLA compliance.
     */
    public void applyEmergencyBoosts() {
        List<Order> currentOrders = List.copyOf(orderQueue);

        for (Order order : currentOrders) {
            priorityScoreService.applyEmergencyBoost(order);
            updatePriorityScore(order);
        }

        // Rebuild queue to reflect new priorities
        reheapifyQueue();
    }
}
