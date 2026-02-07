package com.coffeeShop.Coffee.Shop.dto;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class BaristaResponse {
    private Integer id;
    private String name;
    private boolean available;
    private UUID currentOrderId;
    private Instant orderStartTime;
    private Instant orderEndTime;
    
    // Added for complete state visibility
    private OrderResponse currentOrder;  // Full order details instead of just ID
    private Integer totalWorkedMinutes;  // Total minutes worked (for utilization)
    private Long remainingMinutes;       // Minutes remaining for current order
    private Instant busyUntil;           // When current order will complete
    private boolean busy;                // Explicit busy state
}
