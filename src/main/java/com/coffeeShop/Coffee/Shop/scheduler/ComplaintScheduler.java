package com.coffeeShop.Coffee.Shop.scheduler;

import com.coffeeShop.Coffee.Shop.model.Order;
import com.coffeeShop.Coffee.Shop.service.ComplaintService;
import com.coffeeShop.Coffee.Shop.service.QueueSchedulerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduled task that periodically checks for orders exceeding SLA
 * and automatically creates complaints.
 * 
 * Runs every 30 seconds to check pending orders.
 */
@Slf4j
@Component
public class ComplaintScheduler {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private QueueSchedulerService queueSchedulerService;

    /**
     * Checks all PENDING orders and creates complaints if they exceed 8 minutes.
     * Runs every 30 seconds.
     */
    @Scheduled(fixedDelay = 30000, initialDelay = 10000)
    public void checkAndCreateComplaints() {
        try {
            List<Order> pendingOrders = queueSchedulerService.getAllOrders();

            if (pendingOrders.isEmpty()) {
                return;
            }

            complaintService.checkAndCreateComplaints(pendingOrders);

            int openComplaints = complaintService.getOpenComplaintCount();
            if (openComplaints > 0) {
                log.warn("🚨 Active Complaints: {} open complaints requiring attention", openComplaints);
            }

        } catch (Exception e) {
            log.error("Error during complaint check: {}", e.getMessage(), e);
        }
    }
}
