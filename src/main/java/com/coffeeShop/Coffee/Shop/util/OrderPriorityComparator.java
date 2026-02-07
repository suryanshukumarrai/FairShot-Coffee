package com.coffeeShop.Coffee.Shop.util;

import com.coffeeShop.Coffee.Shop.model.Order;
import com.coffeeShop.Coffee.Shop.service.PriorityScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;

/**
 * Comparator for ordering orders in the priority queue.
 * 
 * This comparator implements the core priority logic:
 * 1. Primary sort: Priority score (descending - higher scores first)
 * 2. Secondary sort: Arrival time (ascending - earlier arrivals first for ties)
 * 
 * The priority score calculation considers:
 * - Wait time (40%): Orders waiting longer get higher priority
 * - Short job bonus (25%): Quick orders are prioritized to reduce overall wait time
 * - Loyalty boost (10%): Gold customers get priority
 * - Urgency factor (25%): Emergency boost for orders approaching 10-minute limit
 * 
 * This approach beats FIFO by:
 * - Reducing average wait time through short job prioritization
 * - Ensuring no order exceeds 10-minute wait limit
 * - Providing premium service to loyal customers
 * - Dynamically adapting to real-time conditions
 */
@Component
public class OrderPriorityComparator implements Comparator<Order> {
    
    private final PriorityScoreService priorityScoreService;
    
    @Autowired
    public OrderPriorityComparator(PriorityScoreService priorityScoreService) {
        this.priorityScoreService = priorityScoreService;
    }
    
    @Override
    public int compare(Order o1, Order o2) {
        try {
            // Calculate priority scores for both orders
            double score1 = priorityScoreService.calculatePriorityScore(o1);
            double score2 = priorityScoreService.calculatePriorityScore(o2);
            
            // Primary comparison: priority score (descending - higher scores first)
            int scoreComparison = Double.compare(score2, score1);
            if (scoreComparison != 0) {
                return scoreComparison;
            }
            
            // Secondary comparison: arrival time (ascending - earlier arrivals first)
            // This ensures FIFO behavior for orders with equal priority
            return o1.getArrivalTime().compareTo(o2.getArrivalTime());
            
        } catch (Exception e) {
            // Fallback to arrival time if scoring fails
            // This ensures the system remains functional even with scoring errors
            return o1.getArrivalTime().compareTo(o2.getArrivalTime());
        }
    }
}
