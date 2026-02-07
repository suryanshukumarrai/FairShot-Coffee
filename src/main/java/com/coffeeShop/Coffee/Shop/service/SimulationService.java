package com.coffeeShop.Coffee.Shop.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.coffeeShop.Coffee.Shop.dto.SimulationRequest;
import com.coffeeShop.Coffee.Shop.dto.SimulationResponse;
import com.coffeeShop.Coffee.Shop.model.Barista;
import com.coffeeShop.Coffee.Shop.model.CustomerType;
import com.coffeeShop.Coffee.Shop.model.DrinkType;
import com.coffeeShop.Coffee.Shop.model.Order;
import com.coffeeShop.Coffee.Shop.model.OrderStatus;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for running simulations to demonstrate system capabilities.
 * 
 * Simulation mode allows instant processing of large numbers of orders
 * to showcase scalability, fairness, and SLA handling without real-time delays.
 * 
 * This is a DEMO/ANALYSIS feature, not production behavior.
 */
@Service
@Slf4j
public class SimulationService {

    private final QueueSchedulerService queueSchedulerService;
    private final BaristaAssignmentService baristaAssignmentService;
    private final OrderCompletionService orderCompletionService;
    private final ManagerMetricsService managerMetricsService;
    
    @Value("${coffee.shop.simulation.enabled:false}")
    private boolean simulationEnabled;

    private final Random random = new Random();
    private final List<String> transparencyMessages = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger fairnessSkipCounter = new AtomicInteger(0);
    private final AtomicInteger slaBreachCounter = new AtomicInteger(0);
    private final AtomicInteger maxQueueSizeTracker = new AtomicInteger(0);

    @Autowired
    public SimulationService(QueueSchedulerService queueSchedulerService,
                           BaristaAssignmentService baristaAssignmentService,
                           OrderCompletionService orderCompletionService,
                           ManagerMetricsService managerMetricsService) {
        this.queueSchedulerService = queueSchedulerService;
        this.baristaAssignmentService = baristaAssignmentService;
        this.orderCompletionService = orderCompletionService;
        this.managerMetricsService = managerMetricsService;
    }

    /**
     * Runs a simulation with the specified parameters.
     * 
     * @param request Simulation parameters
     * @return Simulation results with metrics
     */
    public SimulationResponse runSimulation(SimulationRequest request) {
        if (!simulationEnabled) {
            throw new IllegalStateException("Simulation mode is not enabled. Set coffee.shop.simulation.enabled=true");
        }

        long startTime = System.currentTimeMillis();
        
        // Reset counters
        transparencyMessages.clear();
        fairnessSkipCounter.set(0);
        slaBreachCounter.set(0);
        maxQueueSizeTracker.set(0);
        
        // Track initial metrics
        int initialCoffeesServed = managerMetricsService.getTotalCoffeesServed();
        
        log.info("🎯 SIMULATION START: Creating {} orders", request.getOrders());
        transparencyMessages.add(String.format("🎯 Simulation started: %d orders", request.getOrders()));
        
        // Phase 1: Create all orders
        for (int i = 0; i < request.getOrders(); i++) {
            Order order = createRandomOrder();
            queueSchedulerService.addOrder(order);
            
            // Track max queue size
            int currentQueueSize = queueSchedulerService.getQueueSize();
            maxQueueSizeTracker.set(Math.max(maxQueueSizeTracker.get(), currentQueueSize));
        }
        
        log.info("✅ Created {} orders, max queue size: {}", request.getOrders(), maxQueueSizeTracker.get());
        transparencyMessages.add(String.format("✅ Created %d orders", request.getOrders()));
        transparencyMessages.add(String.format("📊 Max queue size reached: %d", maxQueueSizeTracker.get()));
        
        // Phase 2: Process all orders instantly
        int processedCount = 0;
        int maxIterations = request.getOrders() * 2; // Safety limit
        int iteration = 0;
        
        while (queueSchedulerService.getQueueSize() > 0 && iteration < maxIterations) {
            // Trigger assignment for all available baristas
            baristaAssignmentService.tryAssignNextOrder();
            
            // Instantly complete all orders that are in progress
            List<Barista> baristas = baristaAssignmentService.getAllBaristas();
            for (Barista barista : baristas) {
                if (barista.getCurrentOrder() != null) {
                    Order order = barista.getCurrentOrder();
                    instantCompleteOrder(order, barista);
                    processedCount++;
                    
                    // Track fairness and SLA
                    if (order.getSkipCount() > 0) {
                        fairnessSkipCounter.incrementAndGet();
                    }
                    if (order.getWaitTimeMinutes() >= 8) {
                        slaBreachCounter.incrementAndGet();
                    }
                }
            }
            
            iteration++;
            
            // Log progress every 50 orders
            if (processedCount % 50 == 0 && processedCount > 0) {
                log.info("⏳ Processed {} / {} orders...", processedCount, request.getOrders());
            }
        }
        
        // Generate transparency messages based on results
        if (fairnessSkipCounter.get() > 0) {
            transparencyMessages.add(String.format("⚖️ %d orders experienced fairness skips", fairnessSkipCounter.get()));
            transparencyMessages.add("Fairness penalty applied after 3 skips");
        }
        
        if (slaBreachCounter.get() > 0) {
            transparencyMessages.add(String.format("⚠️ %d SLA breaches (>8 min wait)", slaBreachCounter.get()));
            transparencyMessages.add("Emergency priority activated at 8 min");
        } else {
            transparencyMessages.add("✅ No SLA violations - all orders within 8 minutes");
        }
        
        if (maxQueueSizeTracker.get() > 100) {
            transparencyMessages.add("🚀 Emergency boost activated for high load");
        }
        
        // Calculate barista workload
        Map<Integer, Integer> baristaWorkload = new ConcurrentHashMap<>();
        for (Barista barista : baristaAssignmentService.getAllBaristas()) {
            baristaWorkload.put(barista.getId(), barista.getTotalWorkedMinutes());
        }
        
        long executionTime = System.currentTimeMillis() - startTime;
        
        // Get final metrics
        int finalCoffeesServed = managerMetricsService.getTotalCoffeesServed();
        int ordersProcessed = finalCoffeesServed - initialCoffeesServed;
        
        SimulationResponse response = SimulationResponse.builder()
                .ordersProcessed(ordersProcessed)
                .maxQueueSize(maxQueueSizeTracker.get())
                .avgWaitMinutes(managerMetricsService.getAverageWaitMinutes())
                .fairnessSkips(fairnessSkipCounter.get())
                .slaBreaches(slaBreachCounter.get())
                .baristaWorkload(baristaWorkload)
                .executionTimeMs(executionTime)
                .transparencyMessages(new ArrayList<>(transparencyMessages))
                .build();
        
        log.info("🎉 SIMULATION COMPLETE: {} orders processed in {} ms", ordersProcessed, executionTime);
        transparencyMessages.add(String.format("🎉 Simulation completed in %d ms", executionTime));
        
        return response;
    }

    /**
     * Creates a random order for simulation.
     */
    private Order createRandomOrder() {
        DrinkType[] drinkTypes = DrinkType.values();
        CustomerType[] customerTypes = CustomerType.values();
        
        Order order = Order.builder()
                .id(UUID.randomUUID())
                .arrivalTime(Instant.now())
                .drinkType(drinkTypes[random.nextInt(drinkTypes.length)])
                .customerType(customerTypes[random.nextInt(customerTypes.length)])
                .status(OrderStatus.PENDING)
                .skipCount(0)
                .isUrgent(false)
                .build();
        
        return order;
    }

    /**
     * Instantly completes an order without waiting for prep time.
     * Used only in simulation mode.
     */
    private void instantCompleteOrder(Order order, Barista barista) {
        try {
            // Mark order as completed
            order.setStatus(OrderStatus.COMPLETED);
            order.setCompletionTime(Instant.now());
            
            // Track metrics
            managerMetricsService.incrementCoffeesServed(order.getDrinkType());
            managerMetricsService.recordWaitTime(
                java.time.Duration.between(order.getArrivalTime(), Instant.now())
            );
            
            long waitMinutes = order.getWaitTimeMinutes();
            if (waitMinutes >= 8) {
                managerMetricsService.incrementSlaViolations();
            }
            
            managerMetricsService.recordBaristaWork(barista.getId(), order.getPrepTimeMinutes());
            
            // Free the barista
            barista.completeOrder();
            
        } catch (Exception e) {
            log.error("Error instantly completing order {}: {}", order.getId(), e.getMessage(), e);
        }
    }

    /**
     * Checks if simulation mode is enabled.
     */
    public boolean isSimulationEnabled() {
        return simulationEnabled;
    }

    /**
     * Resets the entire system - CRITICAL for preventing ghost completions.
     * Clears all orders, barista assignments, and scheduled completions.
     */
    public void resetSystem() {
        log.info("🧹 SYSTEM RESET: Clearing all state...");
        
        // 1. Cancel ALL scheduled completions (CRITICAL - prevents ghost completions)
        orderCompletionService.cancelAll();
        log.info("✅ Cancelled all scheduled completions");
        
        // 2. Clear the order queue
        queueSchedulerService.clearQueue();
        log.info("✅ Cleared order queue");
        
        // 3. Reset all baristas (clear currentOrder, reset workload)
        baristaAssignmentService.resetAllBaristas();
        log.info("✅ Reset all baristas");
        
        // 4. Reset metrics
        managerMetricsService.resetMetrics();
        log.info("✅ Reset metrics");
        
        log.info("🎉 SYSTEM RESET complete - all state cleared");
    }
}
