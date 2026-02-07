package com.coffeeShop.Coffee.Shop.service;

import com.coffeeShop.Coffee.Shop.model.Order;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks fairness in order processing to ensure no order is perpetually bypassed.
 * 
 * This service maintains:
 * 1. Skip count for each order (how many later-arrived orders were processed before it)
 * 2. Fairness penalties for orders that have been skipped too many times
 * 3. Thread-safe tracking using concurrent data structures
 * 
 * Fairness is critical because:
 * - Pure priority-based systems can starve low-priority orders
 * - Customers expect reasonable service regardless of order type
 * - System must maintain perceived fairness to retain customer trust
 */
@Service
public class FairnessTrackerService {
    
    private static final int MAX_SKIPS_BEFORE_PENALTY = 3;
    private static final double FAIRNESS_PENALTY_AMOUNT = 20.0;
    
    // Thread-safe tracking of order skip counts
    private final Map<UUID, AtomicInteger> orderSkipCounts = new ConcurrentHashMap<>();
    
    // Track when orders were added to detect bypassing
    private final Map<UUID, Instant> orderAdditionTimes = new ConcurrentHashMap<>();
    
    /**
     * Records that an order has been added to the queue.
     * Initializes tracking for fairness calculations.
     */
    public void recordOrderAdded(Order order) {
        UUID orderId = order.getId();
        orderSkipCounts.put(orderId, new AtomicInteger(0));
        orderAdditionTimes.put(orderId, Instant.now());
    }
    
    /**
     * Records that an order has been processed (removed from queue).
     * Cleans up tracking data to prevent memory leaks.
     */
    public void recordOrderProcessed(Order order) {
        UUID orderId = order.getId();
        orderSkipCounts.remove(orderId);
        orderAdditionTimes.remove(orderId);
    }
    
    /**
     * Increments skip count for all orders that were added before the processed order
     * but were not selected. This is the core fairness tracking mechanism.
     * 
     * @param processedOrder The order that was actually processed
     * @param allPendingOrders All orders currently in the queue
     */
    public void recordOrderProcessedWithFairnessUpdate(Order processedOrder, java.util.List<Order> allPendingOrders) {
        Instant processedOrderTime = orderAdditionTimes.get(processedOrder.getId());
        
        for (Order pendingOrder : allPendingOrders) {
            UUID pendingOrderId = pendingOrder.getId();
            Instant pendingOrderTime = orderAdditionTimes.get(pendingOrderId);
            
            // If pending order was added before processed order, it was "skipped"
            if (pendingOrderTime != null && pendingOrderTime.isBefore(processedOrderTime)) {
                AtomicInteger skipCount = orderSkipCounts.get(pendingOrderId);
                if (skipCount != null) {
                    skipCount.incrementAndGet();
                }
            }
        }
    }
    
    /**
     * Calculates fairness penalty for an order based on skip count.
     * Orders skipped more than 3 times get a -20 point penalty.
     */
    public double calculateFairnessPenalty(Order order) {
        UUID orderId = order.getId();
        AtomicInteger skipCount = orderSkipCounts.get(orderId);
        
        if (skipCount == null) {
            return 0.0;
        }
        
        int skips = skipCount.get();
        if (skips > MAX_SKIPS_BEFORE_PENALTY) {
            return FAIRNESS_PENALTY_AMOUNT;
        }
        
        return 0.0;
    }
    
    /**
     * Gets the current skip count for an order.
     * Useful for monitoring and debugging fairness issues.
     */
    public int getSkipCount(Order order) {
        UUID orderId = order.getId();
        AtomicInteger skipCount = orderSkipCounts.get(orderId);
        return skipCount != null ? skipCount.get() : 0;
    }
    
    /**
     * Resets skip count for an order (used when order is finally processed).
     * This ensures fairness tracking is accurate over time.
     */
    public void resetSkipCount(Order order) {
        UUID orderId = order.getId();
        AtomicInteger skipCount = orderSkipCounts.get(orderId);
        if (skipCount != null) {
            skipCount.set(0);
        }
    }
    
    /**
     * Gets statistics for monitoring fairness across the system.
     * Helps identify if the system is maintaining reasonable fairness.
     */
    public FairnessStatistics getFairnessStatistics() {
        int totalOrders = orderSkipCounts.size();
        int ordersWithSkips = 0;
        int totalSkips = 0;
        int ordersWithPenalty = 0;
        
        for (AtomicInteger skipCount : orderSkipCounts.values()) {
            int skips = skipCount.get();
            if (skips > 0) {
                ordersWithSkips++;
                totalSkips += skips;
            }
            if (skips > MAX_SKIPS_BEFORE_PENALTY) {
                ordersWithPenalty++;
            }
        }
        
        return new FairnessStatistics(totalOrders, ordersWithSkips, totalSkips, ordersWithPenalty);
    }
    
    /**
     * Data class for fairness statistics.
     */
    public static class FairnessStatistics {
        public final int totalOrders;
        public final int ordersWithSkips;
        public final int totalSkips;
        public final int ordersWithPenalty;
        
        public FairnessStatistics(int totalOrders, int ordersWithSkips, int totalSkips, int ordersWithPenalty) {
            this.totalOrders = totalOrders;
            this.ordersWithSkips = ordersWithSkips;
            this.totalSkips = totalSkips;
            this.ordersWithPenalty = ordersWithPenalty;
        }
        
        public double getAverageSkips() {
            return totalOrders > 0 ? (double) totalSkips / totalOrders : 0;
        }
        
        public double getPenaltyRate() {
            return totalOrders > 0 ? (double) ordersWithPenalty / totalOrders : 0;
        }
    }
}
