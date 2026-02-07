package com.coffeeShop.Coffee.Shop.service;

import com.coffeeShop.Coffee.Shop.model.Complaint;
import com.coffeeShop.Coffee.Shop.model.ComplaintStatus;
import com.coffeeShop.Coffee.Shop.model.Order;
import com.coffeeShop.Coffee.Shop.model.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages complaint creation and tracking for orders exceeding SLA.
 * 
 * This service:
 * 1. Monitors all PENDING orders for 8+ minute threshold
 * 2. Auto-creates complaints when threshold is exceeded
 * 3. Marks orders as URGENT for priority handling
 * 4. Provides complaint history and management API
 */
@Slf4j
@Service
public class ComplaintService {

    private final Map<UUID, Complaint> complaints = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> complaintCreatedForOrder = new ConcurrentHashMap<>();

    /**
     * Checks all pending orders and creates complaints if needed.
     * Called periodically by scheduler.
     * 
     * @param pendingOrders List of all PENDING orders
     */
    public void checkAndCreateComplaints(List<Order> pendingOrders) {
        for (Order order : pendingOrders) {
            if (order.requiresComplaint() && !hasComplaint(order.getId())) {
                createComplaint(order, "Exceeded preparation SLA (" + Order.COMPLAINT_THRESHOLD_MINUTES + " minutes)");
            }
        }
    }

    /**
     * Creates a complaint for an order.
     * 
     * @param order  The order exceeding SLA
     * @param reason Human-readable reason
     * @return Created complaint
     */
    public Complaint createComplaint(Order order, String reason) {
        // Prevent duplicate complaints
        if (complaintCreatedForOrder.containsKey(order.getId())) {
            log.debug("Complaint already exists for order {}", order.getId());
            return complaints.values().stream()
                    .filter(c -> c.getOrderId().equals(order.getId()))
                    .findFirst()
                    .orElse(null);
        }

        Complaint complaint = Complaint.builder()
                .id(UUID.randomUUID())
                .orderId(order.getId())
                .createdAt(Instant.now())
                .waitTimeMinutes(order.getWaitTimeMinutes())
                .reason(reason)
                .status(ComplaintStatus.OPEN)
                .build();

        complaints.put(complaint.getId(), complaint);
        complaintCreatedForOrder.put(order.getId(), true);

        // Mark order as urgent
        order.markAsUrgent();

        log.warn("🚨 COMPLAINT CREATED: Order {} has waited {} minutes - {}",
                order.getId(),
                String.format("%.1f", complaint.getWaitTimeMinutes()),
                reason);

        return complaint;
    }

    /**
     * Checks if a complaint exists for an order.
     */
    public boolean hasComplaint(UUID orderId) {
        return complaintCreatedForOrder.containsKey(orderId);
    }

    /**
     * Gets all open complaints.
     */
    public List<Complaint> getOpenComplaints() {
        return complaints.values().stream()
                .filter(Complaint::isOpen)
                .collect(Collectors.toList());
    }

    /**
     * Gets all complaints (open and resolved).
     */
    public List<Complaint> getAllComplaints() {
        return new ArrayList<>(complaints.values());
    }

    /**
     * Gets complaints for a specific order.
     */
    public List<Complaint> getComplaintsForOrder(UUID orderId) {
        return complaints.values().stream()
                .filter(c -> c.getOrderId().equals(orderId))
                .collect(Collectors.toList());
    }

    /**
     * Resolves a complaint when the order is completed or addressed.
     */
    public void resolveComplaint(UUID complaintId) {
        Complaint complaint = complaints.get(complaintId);
        if (complaint != null && complaint.isOpen()) {
            complaint.resolve();
            log.info("Complaint {} resolved for order {}", complaintId, complaint.getOrderId());
        }
    }

    /**
     * Auto-resolves complaints when an order is completed.
     */
    public void autoResolveComplaintsForOrder(UUID orderId) {
        complaints.values().stream()
                .filter(c -> c.getOrderId().equals(orderId) && c.isOpen())
                .forEach(c -> {
                    c.resolve();
                    log.info("Auto-resolved complaint {} for completed order {}", c.getId(), orderId);
                });
    }

    /**
     * Gets total complaint count (for monitoring).
     */
    public int getTotalComplaintCount() {
        return complaints.size();
    }

    /**
     * Gets open complaint count (for dashboard).
     */
    public int getOpenComplaintCount() {
        return (int) complaints.values().stream()
                .filter(Complaint::isOpen)
                .count();
    }
}
