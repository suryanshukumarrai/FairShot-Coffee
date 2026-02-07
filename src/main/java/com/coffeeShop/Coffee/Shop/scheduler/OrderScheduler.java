package com.coffeeShop.Coffee.Shop.scheduler;

import com.coffeeShop.Coffee.Shop.model.Order;
import com.coffeeShop.Coffee.Shop.service.FairnessTrackerService;
import com.coffeeShop.Coffee.Shop.service.QueueSchedulerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Scheduled tasks for maintaining optimal queue performance.
 * 
 * This scheduler runs periodic maintenance tasks to ensure:
 * 1. Priority scores reflect current conditions (every 30 seconds)
 * 2. Emergency boosts are applied to urgent orders
 * 3. Queue is reorganized with updated priorities
 * 4. Fairness metrics are monitored
 * 
 * The 30-second interval balances:
 * - Responsiveness to changing conditions
 * - Computational overhead
 * - System stability
 */
@Component
@Slf4j
@org.springframework.context.annotation.DependsOn("baristaAssignmentService")
public class OrderScheduler {

    private final QueueSchedulerService queueSchedulerService;
    private final FairnessTrackerService fairnessTrackerService;

    @Autowired
    public OrderScheduler(QueueSchedulerService queueSchedulerService,
            FairnessTrackerService fairnessTrackerService) {
        this.queueSchedulerService = queueSchedulerService;
        this.fairnessTrackerService = fairnessTrackerService;
    }

    /**
     * Main priority recalculation task.
     * Runs every 30 seconds to maintain optimal queue ordering.
     * 
     * This task is critical because:
     * - Wait times continuously increase, affecting priority scores
     * - Orders approaching 8-minute wait need emergency boosting
     * - Fairness penalties may need to be applied
     * - Queue must be re-sorted with updated scores
     */
    @Scheduled(fixedDelay = 30000, initialDelay = 10000)
    public void recalculatePriorityScores() {
        log.info("PRIORITY RECALCULATION: Starting priority score recalculation...");

        try {
            List<Order> allOrders = queueSchedulerService.getAllOrders();

            if (allOrders.isEmpty()) {
                log.info("PRIORITY RECALCULATION: No orders in shared queue to recalculate");
                return;
            }

            log.info("PRIORITY RECALCULATION: Processing {} orders in shared queue", allOrders.size());

            int ordersRequiringEmergencyBoost = 0;
            int ordersWithFairnessPenalty = 0;

            // Recalculate priority scores for all orders
            for (Order order : allOrders) {
                // Check if order requires emergency boost
                if (requiresEmergencyBoost(order)) {
                    ordersRequiringEmergencyBoost++;
                    log.info("EMERGENCY BOOST: Order {} requires emergency boost (wait time: {} minutes)",
                            order.getId(), getWaitMinutes(order));
                }

                // Check for fairness penalty
                if (fairnessTrackerService.calculateFairnessPenalty(order) > 0) {
                    ordersWithFairnessPenalty++;
                    log.info("FAIRNESS PENALTY: Order {} has fairness penalty (skipped {} times)",
                            order.getId(), fairnessTrackerService.getSkipCount(order));
                }

                // Update priority score (includes emergency boost and fairness penalty)
                queueSchedulerService.updatePriorityScore(order);
            }

            // Rebuild queue with updated priorities
            queueSchedulerService.reheapifyQueue();

            log.info(
                    "PRIORITY RECALCULATION COMPLETED: Orders processed: {}, Emergency boosts: {}, Fairness penalties: {}",
                    allOrders.size(), ordersRequiringEmergencyBoost, ordersWithFairnessPenalty);

            // Log fairness statistics
            logFairnessStatistics();

        } catch (Exception e) {
            log.error("Error during priority score recalculation: {}", e.getMessage(), e);
        }
    }

    /**
     * Checks if an order requires emergency boost based on wait time.
     * Orders waiting 8+ minutes get maximum priority to prevent SLA violations.
     */
    private boolean requiresEmergencyBoost(Order order) {
        long waitMinutes = getWaitMinutes(order);
        return waitMinutes >= 8;
    }

    /**
     * Calculates wait time in minutes for an order.
     */
    private long getWaitMinutes(Order order) {
        return Duration.between(order.getArrivalTime(), Instant.now()).toMinutes();
    }

    /**
     * Logs fairness statistics for monitoring system health.
     * Helps identify if the fairness mechanism is working correctly.
     */
    private void logFairnessStatistics() {
        try {
            FairnessTrackerService.FairnessStatistics stats = fairnessTrackerService.getFairnessStatistics();

            if (stats.totalOrders > 0) {
                log.info(
                        "Fairness Statistics - Total orders: {}, Orders with skips: {}, Average skips: {:.2f}, Penalty rate: {:.2f}%",
                        stats.totalOrders,
                        stats.ordersWithSkips,
                        stats.getAverageSkips(),
                        stats.getPenaltyRate() * 100);
            }

        } catch (Exception e) {
            log.error("Error logging fairness statistics: {}", e.getMessage(), e);
        }
    }

    /**
     * Additional task to monitor queue health and performance.
     * Runs every 60 seconds to provide operational insights.
     */
    @Scheduled(fixedDelay = 60000, initialDelay = 30000)
    public void monitorQueueHealth() {
        try {
            int queueSize = queueSchedulerService.getQueueSize();

            if (queueSize > 0) {
                List<Order> orders = queueSchedulerService.getAllOrders();

                // Calculate statistics
                long maxWaitTime = 0;
                long totalWaitTime = 0;
                int urgentOrders = 0;

                for (Order order : orders) {
                    long waitTime = getWaitMinutes(order);
                    maxWaitTime = Math.max(maxWaitTime, waitTime);
                    totalWaitTime += waitTime;

                    if (waitTime >= 8) {
                        urgentOrders++;
                    }
                }

                double averageWaitTime = (double) totalWaitTime / orders.size();

                log.info("Queue Health - Size: {}, Avg wait: {:.1f} min, Max wait: {} min, Urgent orders: {}",
                        queueSize, averageWaitTime, maxWaitTime, urgentOrders);

                // Alert if any order is approaching the 10-minute limit
                if (maxWaitTime >= 9) {
                    log.warn("CRITICAL: Orders approaching 10-minute wait limit! Max wait: {} minutes", maxWaitTime);
                }
            }

        } catch (Exception e) {
            log.error("Error during queue health monitoring: {}", e.getMessage(), e);
        }
    }
}
