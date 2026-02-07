package com.coffeeShop.Coffee.Shop.config;

import com.coffeeShop.Coffee.Shop.model.Order;
import com.coffeeShop.Coffee.Shop.util.OrderPriorityComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.PriorityBlockingQueue;

/**
 * Spring configuration for shared beans.
 * 
 * This configuration ensures all components use the SAME PriorityBlockingQueue
 * instance, preventing the shared-state bug where orders were created but
 * not visible across services.
 */
@Configuration
public class CoffeeShopConfig {
    
    private final OrderPriorityComparator orderPriorityComparator;
    
    @Autowired
    public CoffeeShopConfig(OrderPriorityComparator orderPriorityComparator) {
        this.orderPriorityComparator = orderPriorityComparator;
    }
    
    /**
     * Single shared PriorityBlockingQueue for all order operations.
     * 
     * This bean is the SINGLE source of truth for the order queue.
     * All services must use this queue to ensure orders are visible
     * across the entire application.
     */
    @Bean
    public PriorityBlockingQueue<Order> orderQueue() {
        return new PriorityBlockingQueue<>(100, orderPriorityComparator);
    }
}
