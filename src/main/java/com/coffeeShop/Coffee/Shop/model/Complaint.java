package com.coffeeShop.Coffee.Shop.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Complaint {
    private UUID id;
    private UUID orderId;
    private Instant createdAt;
    private double waitTimeMinutes;
    private String reason;

    @Builder.Default
    private ComplaintStatus status = ComplaintStatus.OPEN;

    public void resolve() {
        this.status = ComplaintStatus.RESOLVED;
    }

    public boolean isOpen() {
        return this.status == ComplaintStatus.OPEN;
    }
}
