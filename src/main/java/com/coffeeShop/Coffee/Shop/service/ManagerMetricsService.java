package com.coffeeShop.Coffee.Shop.service;

import com.coffeeShop.Coffee.Shop.model.DrinkType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for tracking manager-level analytics and metrics.
 * 
 * Thread-safe singleton that maintains real-time statistics about
 * coffee shop operations for manager dashboard and analysis.
 */
@Service
@Slf4j
public class ManagerMetricsService {

    // Total coffees served counter
    private final AtomicInteger totalCoffeesServed = new AtomicInteger(0);

    // Coffees served by drink type
    private final Map<DrinkType, AtomicInteger> coffeesByDrink = new ConcurrentHashMap<>();

    // Wait time tracking
    private final AtomicLong totalWaitTimeSeconds = new AtomicLong(0);
    private final AtomicInteger completedOrdersCount = new AtomicInteger(0);

    // SLA violations
    private final AtomicInteger slaViolations = new AtomicInteger(0);

    // Barista utilization tracking (minutes worked)
    private final Map<Integer, AtomicInteger> baristaWorkedMinutes = new ConcurrentHashMap<>();

    public ManagerMetricsService() {
        // Initialize counters for all drink types
        for (DrinkType drinkType : DrinkType.values()) {
            coffeesByDrink.put(drinkType, new AtomicInteger(0));
        }

        log.info("ManagerMetricsService initialized - tracking metrics");
    }

    /**
     * Increments total coffees served counter.
     * Called when an order is completed.
     */
    public void incrementCoffeesServed(DrinkType drinkType) {
        totalCoffeesServed.incrementAndGet();
        coffeesByDrink.get(drinkType).incrementAndGet();

        log.debug("Metrics updated: total={}, drink={}",
                totalCoffeesServed.get(), drinkType);
    }

    /**
     * Records wait time for a completed order.
     * 
     * @param waitDuration Duration from arrival to completion
     */
    public void recordWaitTime(Duration waitDuration) {
        totalWaitTimeSeconds.addAndGet(waitDuration.getSeconds());
        completedOrdersCount.incrementAndGet();
    }

    /**
     * Increments SLA violation counter.
     * Called when an order exceeds 8 minute threshold.
     */
    public void incrementSlaViolations() {
        int violations = slaViolations.incrementAndGet();
        log.warn("SLA violation recorded - total violations: {}", violations);
    }

    /**
     * Records barista work time.
     * 
     * @param baristaId The barista ID
     * @param minutes   Minutes worked on order
     */
    public void recordBaristaWork(Integer baristaId, int minutes) {
        baristaWorkedMinutes
                .computeIfAbsent(baristaId, k -> new AtomicInteger(0))
                .addAndGet(minutes);
    }

    /**
     * Gets total coffees served.
     */
    public int getTotalCoffeesServed() {
        return totalCoffeesServed.get();
    }

    /**
     * Gets coffees served by drink type.
     */
    public Map<DrinkType, Integer> getCoffeesByDrink() {
        Map<DrinkType, Integer> result = new ConcurrentHashMap<>();
        coffeesByDrink.forEach((type, count) -> result.put(type, count.get()));
        return result;
    }

    /**
     * Calculates average wait time in minutes.
     */
    public double getAverageWaitMinutes() {
        int count = completedOrdersCount.get();
        if (count == 0) {
            return 0.0;
        }

        long totalSeconds = totalWaitTimeSeconds.get();
        return (double) totalSeconds / count / 60.0;
    }

    /**
     * Gets total SLA violations.
     */
    public int getSlaViolations() {
        return slaViolations.get();
    }

    /**
     * Gets barista utilization percentage.
     * Assumes ~ 60 minutes per hour window for simplicity.
     * 
     * @param baristaId The barista ID
     * @return Utilization percentage (0-100)
     */
    public double getBaristaUtilization(Integer baristaId) {
        AtomicInteger worked = baristaWorkedMinutes.get(baristaId);
        if (worked == null) {
            return 0.0;
        }

        // Simple calculation: worked minutes / available minutes * 100
        // Assumes ~60 minute tracking window for demo
        int workedMinutes = worked.get();
        return Math.min(100.0, (workedMinutes / 60.0) * 100);
    }

    /**
     * Gets all barista utilization percentages.
     */
    public Map<Integer, Double> getAllBaristaUtilization() {
        Map<Integer, Double> result = new ConcurrentHashMap<>();
        baristaWorkedMinutes.forEach((id, minutes) -> result.put(id, getBaristaUtilization(id)));
        return result;
    }

    /**
     * Resets all metrics (for testing/demo purposes).
     */
    public void resetMetrics() {
        totalCoffeesServed.set(0);
        totalWaitTimeSeconds.set(0);
        completedOrdersCount.set(0);
        slaViolations.set(0);

        coffeesByDrink.values().forEach(counter -> counter.set(0));
        baristaWorkedMinutes.clear();

        log.info("All metrics reset");
    }
}
