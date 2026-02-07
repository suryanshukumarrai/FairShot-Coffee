package com.coffeeShop.Coffee.Shop.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for simulation endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulationResponse {
    
    private Integer ordersProcessed;
    private Integer maxQueueSize;
    private Double avgWaitMinutes;
    private Integer fairnessSkips;
    private Integer slaBreaches;
    private Map<Integer, Integer> baristaWorkload;
    private Long executionTimeMs;
    private List<String> transparencyMessages;
}
