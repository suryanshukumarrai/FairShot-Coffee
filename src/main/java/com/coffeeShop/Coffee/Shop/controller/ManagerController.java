package com.coffeeShop.Coffee.Shop.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coffeeShop.Coffee.Shop.dto.ManagerMetricsResponse;
import com.coffeeShop.Coffee.Shop.dto.SimulationRequest;
import com.coffeeShop.Coffee.Shop.dto.SimulationResponse;
import com.coffeeShop.Coffee.Shop.model.DrinkType;
import com.coffeeShop.Coffee.Shop.service.ManagerMetricsService;
import com.coffeeShop.Coffee.Shop.service.SimulationService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for manager operations.
 * 
 * Provides endpoints for:
 * 1. Viewing system metrics and analytics
 * 2. Running simulations for demo/testing
 * 3. System configuration and monitoring
 */
@RestController
@RequestMapping("/api/manager")
@Slf4j
public class ManagerController {
    
    private final ManagerMetricsService managerMetricsService;
    private final SimulationService simulationService;
    
    @Autowired
    public ManagerController(ManagerMetricsService managerMetricsService,
                           SimulationService simulationService) {
        this.managerMetricsService = managerMetricsService;
        this.simulationService = simulationService;
    }
    
    /**
     * Gets comprehensive system metrics for manager dashboard.
     * 
     * @return Metrics including total coffees served, by drink type, avg wait time, SLA violations, and barista utilization
     */
    @GetMapping("/metrics")
    public ResponseEntity<ManagerMetricsResponse> getMetrics() {
        try {
            Map<DrinkType, Integer> coffeesByDrink = managerMetricsService.getCoffeesByDrink();
            
            // Convert DrinkType enum keys to displayName strings for frontend
            Map<String, Integer> coffeesByDrinkDisplay = new HashMap<>();
            for (Map.Entry<DrinkType, Integer> entry : coffeesByDrink.entrySet()) {
                coffeesByDrinkDisplay.put(entry.getKey().getDisplayName(), entry.getValue());
            }
            
            ManagerMetricsResponse response = ManagerMetricsResponse.builder()
                    .totalCoffeesServed(managerMetricsService.getTotalCoffeesServed())
                    .byDrink(coffeesByDrinkDisplay)
                    .avgWaitMinutes(managerMetricsService.getAverageWaitMinutes())
                    .slaViolations(managerMetricsService.getSlaViolations())
                    .baristaUtilization(managerMetricsService.getAllBaristaUtilization())
                    .build();
            
            log.debug("Retrieved manager metrics: {} total coffees served", response.getTotalCoffeesServed());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error retrieving manager metrics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Runs a simulation with specified parameters.
     * 
     * @param request Simulation parameters (number of orders, random flag)
     * @return Simulation results with metrics
     */
    @PostMapping("/simulate")
    public ResponseEntity<SimulationResponse> runSimulation(@Valid @RequestBody SimulationRequest request) {
        try {
            log.info("Starting simulation: {} orders, random={}", request.getOrders(), request.isRandom());
            
            SimulationResponse response = simulationService.runSimulation(request);
            
            log.info("Simulation completed: {} orders processed in {} ms", 
                    response.getOrdersProcessed(), response.getExecutionTimeMs());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error running simulation: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Resets all metrics (for testing/demo purposes).
     */
    @PostMapping("/metrics/reset")
    public ResponseEntity<Void> resetMetrics() {
        try {
            managerMetricsService.resetMetrics();
            log.info("Metrics reset by manager");
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            log.error("Error resetting metrics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Resets the entire system - clears all orders, baristas, and scheduled completions.
     * CRITICAL for simulation reset to prevent ghost completions.
     */
    @PostMapping("/reset")
    public ResponseEntity<Void> resetSystem() {
        try {
            log.info("SYSTEM RESET initiated by manager");
            
            simulationService.resetSystem();
            
            log.info("SYSTEM RESET completed successfully");
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            log.error("Error during system reset: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
