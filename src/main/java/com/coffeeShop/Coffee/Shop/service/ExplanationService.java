package com.coffeeShop.Coffee.Shop.service;

import org.springframework.stereotype.Service;

/**
 * Generates human-readable explanations for order state changes.
 * 
 * This service provides transparency to customers about:
 * - Why their order position changed
 * - Why quicker orders were served first
 * - Why they were promoted due to fairness
 */
@Service
public class ExplanationService {

    /**
     * Generates explanation when an order has been skipped.
     * 
     * @param skipCount Number of orders that jumped ahead
     * @return Human-readable explanation
     */
    public String generateSkipExplanation(int skipCount) {
        if (skipCount == 0) {
            return null;
        }
        if (skipCount == 1) {
            return "1 quicker order was served ahead to reduce overall wait time.";
        }
        return skipCount + " quicker orders were served ahead to reduce overall wait time.";
    }

    /**
     * Generates explanation for urgent escalation.
     */
    public String generateUrgencyExplanation() {
        return "Your order exceeded wait time and is now prioritized.";
    }

    /**
     * Generates explanation for fairness penalty promotion.
     */
    public String generateFairnessExplanation() {
        return "Promoted to top of queue due to fairness guarantee (skipped 3+ times).";
    }

    /**
     * Generates explanation for VIP priority.
     */
    public String generateVipExplanation() {
        return "VIP customers receive priority service.";
    }
}
