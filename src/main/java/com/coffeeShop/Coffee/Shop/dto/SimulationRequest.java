package com.coffeeShop.Coffee.Shop.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for simulation endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulationRequest {
    
    @Min(1)
    @Max(500)
    private Integer orders;
    
    @Builder.Default
    private boolean random = true;
}
