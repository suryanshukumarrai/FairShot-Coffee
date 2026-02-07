package com.coffeeShop.Coffee.Shop.dto;

import com.coffeeShop.Coffee.Shop.model.CustomerType;
import com.coffeeShop.Coffee.Shop.model.DrinkType;
import com.coffeeShop.Coffee.Shop.model.OrderStatus;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class OrderResponse {
    private UUID id;
    private Instant arrivalTime;
    private DrinkType drinkType;
    private CustomerType customerType;
    private OrderStatus status;
    private Instant startTime;
    private Instant completionTime;
    private Integer assignedBaristaId;
    private long waitTimeMinutes;
    private boolean overdue;

    // New fields for backend fixes
    private int priceInRupees;
    private int skipCount;
    private String explanation;
    private boolean isUrgent;
}
