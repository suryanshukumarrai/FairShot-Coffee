package com.coffeeShop.Coffee.Shop.scheduler;

import com.coffeeShop.Coffee.Shop.dto.OrderRequest;
import com.coffeeShop.Coffee.Shop.model.CustomerType;
import com.coffeeShop.Coffee.Shop.model.DrinkType;
import com.coffeeShop.Coffee.Shop.model.Order;
import com.coffeeShop.Coffee.Shop.service.QueueSchedulerService;
import com.coffeeShop.Coffee.Shop.util.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Simulates order arrivals for testing the coffee shop system.
 * 
 * This simulator generates orders with Poisson-like distribution to test:
 * 1. Priority queue performance
 * 2. Fairness tracking
 * 3. Emergency handling
 * 4. Workload balancing
 * 
 * Orders are generated every 3 seconds with realistic distribution:
 * - 60% regular customers, 30% new, 10% gold
 * - Varied drink types based on popularity
 * 
 * CRITICAL: All orders are added through QueueSchedulerService to ensure
 * they go into the SHARED PriorityBlockingQueue bean.
 */
@Component
@Slf4j
@org.springframework.context.annotation.DependsOn("baristaAssignmentService")
public class OrderSimulator {

    private final QueueSchedulerService queueSchedulerService;
    private final OrderMapper orderMapper;
    private final Random random = new Random();

    public OrderSimulator(QueueSchedulerService queueSchedulerService,
            OrderMapper orderMapper) {
        this.queueSchedulerService = queueSchedulerService;
        this.orderMapper = orderMapper;
    }

    /**
     * Simulates order arrival every 3 seconds.
     * Starts after 5 seconds to allow system initialization.
     */
    @Scheduled(fixedDelay = 3000, initialDelay = 5000)
    public void simulateOrderArrival() {
        try {
            // Only generate orders if queue is not too full
            if (queueSchedulerService.getQueueSize() < 15) {
                OrderRequest orderRequest = generateRandomOrder();
                Order order = orderMapper.toOrder(orderRequest);

                log.info("SIMULATOR: Generated {} order for {} customer",
                        orderRequest.getDrinkType().getDisplayName(),
                        orderRequest.getCustomerType().getDisplayName());

                // CRITICAL: Add order through shared QueueSchedulerService
                queueSchedulerService.addOrder(order);

                log.info("SIMULATOR: Order {} added to shared queue via QueueSchedulerService", order.getId());
            } else {
                log.debug("SIMULATOR: Queue full ({}), skipping order generation",
                        queueSchedulerService.getQueueSize());
            }

        } catch (Exception e) {
            log.error("Error in order simulation: {}", e.getMessage(), e);
        }
    }

    /**
     * Generates a random order with realistic distribution.
     */
    private OrderRequest generateRandomOrder() {
        OrderRequest orderRequest = new OrderRequest();

        // Set customer type (60% regular, 30% new, 10% gold)
        CustomerType customerType = generateCustomerType();
        orderRequest.setCustomerType(customerType);

        // Set drink type based on popularity
        DrinkType drinkType = generateDrinkType();
        orderRequest.setDrinkType(drinkType);

        return orderRequest;
    }

    /**
     * Generates customer type with weighted distribution.
     */
    private CustomerType generateCustomerType() {
        int rand = random.nextInt(100);

        if (rand < 60) {
            return CustomerType.REGULAR;
        } else if (rand < 90) {
            return CustomerType.NEW;
        } else {
            return CustomerType.GOLD;
        }
    }

    /**
     * Generates drink type based on realistic coffee shop distribution.
     */
    private DrinkType generateDrinkType() {
        int rand = random.nextInt(100);

        // Realistic distribution:
        // 25% Cold Brew, 20% Espresso, 20% Americano, 15% Cappuccino, 15% Latte, 5%
        // Specialty
        if (rand < 25) {
            return DrinkType.COLD_BREW;
        } else if (rand < 45) {
            return DrinkType.ESPRESSO;
        } else if (rand < 65) {
            return DrinkType.AMERICANO;
        } else if (rand < 80) {
            return DrinkType.CAPPUCCINO;
        } else if (rand < 95) {
            return DrinkType.LATTE;
        } else {
            return DrinkType.MOCHA;
        }
    }
}
