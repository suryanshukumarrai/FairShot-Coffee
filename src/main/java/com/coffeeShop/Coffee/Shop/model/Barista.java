package com.coffeeShop.Coffee.Shop.model;

import java.time.Duration;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Barista {
    private Integer id;
    private String name;
    private Instant busyUntil;
    private Order currentOrder;
    private Integer totalWorkedMinutes;

    /**
     * Checks if barista is available for new orders.
     * 
     * CRITICAL: This is the ONLY source of truth for availability.
     * State-based (not time-based) to ensure deterministic behavior.
     * 
     * @return true if barista has no current order, false otherwise
     */
    public boolean isAvailable() {
        return currentOrder == null;
    }

    /**
     * Checks if barista is currently working on an order.
     */
    public boolean isBusy() {
        return currentOrder != null;
    }

    /**
     * Assigns an order to this barista.
     * Sets the barista as busy with the given order.
     * 
     * Thread-safe and validates state before assignment.
     * 
     * CRITICAL INVARIANT: Order MUST be IN_PROGRESS before assignment.
     */
    public synchronized void assignOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Cannot assign null order");
        }
        if (!isAvailable()) {
            throw new IllegalStateException("Barista is already busy with order: " + currentOrder.getId());
        }
        
        // ENFORCE INVARIANT: Order must be IN_PROGRESS when assigned
        if (order.getStatus() != OrderStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot assign order that is not IN_PROGRESS. Current status: " + order.getStatus());
        }

        this.currentOrder = order;
        this.busyUntil = Instant.now().plusSeconds(order.getPrepTimeMinutes() * 60L);
    }

    /**
     * Completes the current order and frees the barista.
     * 
     * Idempotent - safe to call multiple times.
     * Thread-safe with synchronized modifier.
     */
    public synchronized void completeOrder() {
        if (currentOrder == null) {
            // Already completed or no order - idempotent
            return;
        }

        // Track workload (use prep time for simplicity)
        int prepMinutes = currentOrder.getPrepTimeMinutes();
        if (this.totalWorkedMinutes == null) {
            this.totalWorkedMinutes = 0;
        }
        this.totalWorkedMinutes += prepMinutes;

        // Free the barista (ONLY place where currentOrder is set to null)
        this.currentOrder = null;
        this.busyUntil = null;
    }

    /**
     * Force reset barista state - used for simulation reset.
     * Clears current order and resets all state.
     */
    public synchronized void forceReset() {
        this.currentOrder = null;
        this.busyUntil = null;
        this.totalWorkedMinutes = 0;
    }

    /**
     * Gets the remaining time for current order in minutes.
     */
    public long getRemainingMinutes() {
        if (busyUntil == null) {
            return 0;
        }
        return Math.max(0, Duration.between(Instant.now(), busyUntil).toMinutes());
    }
}
