package com.coffeeShop.Coffee.Shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO for manager metrics endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerMetricsResponse {
    private Integer totalCoffeesServed;
    private Map<String, Integer> byDrink;
    private Double avgWaitMinutes;
    private Integer slaViolations;
    private Map<Integer, Double> baristaUtilization;
}
