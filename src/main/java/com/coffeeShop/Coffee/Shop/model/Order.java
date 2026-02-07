package com.coffeeShop.Coffee.Shop.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private UUID id;
    private Instant arrivalTime;
    private DrinkType drinkType;
    private CustomerType customerType;
    private OrderStatus status;
    private Instant startTime;
    private Instant completionTime;
    private Integer assignedBaristaId;

    // Fairness tracking
    @Builder.Default
    private int skipCount = 0;

    // Explanation for customer
    private String explanation;

    // Emergency escalation
    @Builder.Default
    private boolean isUrgent = false;

    public static final int MAX_WAIT_MINUTES = 10;
    public static final int COMPLAINT_THRESHOLD_MINUTES = 8;
    public static final int FAIRNESS_SKIP_LIMIT = 3;

    public int getPrepTimeMinutes() {
        return drinkType.getPrepTimeMinutes();
    }

    public int getPriceInRupees() {
        return drinkType.getPriceInRupees();
    }

    public boolean isOverdue() {
        return arrivalTime.plusSeconds(MAX_WAIT_MINUTES * 60L).isBefore(Instant.now());
    }

    public long getWaitTimeMinutes() {
        Instant now = Instant.now();
        if (startTime != null) {
            return Duration.between(arrivalTime, startTime).toMinutes();
        }
        return Duration.between(arrivalTime, now).toMinutes();
    }

    public void incrementSkipCount() {
        this.skipCount++;
    }

    public boolean shouldApplyFairnessPenalty() {
        return skipCount >= FAIRNESS_SKIP_LIMIT;
    }

    public void markAsUrgent() {
        this.isUrgent = true;
    }

    public boolean requiresComplaint() {
        return status == OrderStatus.PENDING && getWaitTimeMinutes() >= COMPLAINT_THRESHOLD_MINUTES;
    }
}
