package com.coffeeShop.Coffee.Shop.controller;

import com.coffeeShop.Coffee.Shop.model.Complaint;
import com.coffeeShop.Coffee.Shop.service.ComplaintService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST API controller for complaint management.
 * Provides endpoints for managers to view and resolve complaints.
 */
@Slf4j
@RestController
@RequestMapping("/api/complaints")
@CrossOrigin(origins = "*")
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    /**
     * Gets all complaints (open and resolved).
     * 
     * @return List of all complaints
     */
    @GetMapping
    public List<Complaint> getAllComplaints() {
        log.info("GET /api/complaints - fetching all complaints");
        return complaintService.getAllComplaints();
    }

    /**
     * Gets only open (unresolved) complaints.
     * Used by manager dashboard to show active issues.
     * 
     * @return List of open complaints
     */
    @GetMapping("/open")
    public List<Complaint> getOpenComplaints() {
        log.info("GET /api/complaints/open - fetching open complaints");
        return complaintService.getOpenComplaints();
    }

    /**
     * Gets complaints for a specific order.
     * 
     * @param orderId Order UUID
     * @return List of complaints for the order
     */
    @GetMapping("/order/{orderId}")
    public List<Complaint> getComplaintsForOrder(@PathVariable UUID orderId) {
        log.info("GET /api/complaints/order/{} - fetching complaints for order", orderId);
        return complaintService.getComplaintsForOrder(orderId);
    }

    /**
     * Resolves a complaint (marks as resolved).
     * 
     * @param id Complaint UUID
     */
    @PutMapping("/{id}/resolve")
    public void resolveComplaint(@PathVariable UUID id) {
        log.info("PUT /api/complaints/{}/resolve - resolving complaint", id);
        complaintService.resolveComplaint(id);
    }

    /**
     * Gets complaint statistics for monitoring.
     */
    @GetMapping("/stats")
    public ComplaintStats getStats() {
        return new ComplaintStats(
                complaintService.getTotalComplaintCount(),
                complaintService.getOpenComplaintCount());
    }

    /**
     * Simple DTO for complaint statistics.
     */
    public record ComplaintStats(int total, int open) {
    }
}
