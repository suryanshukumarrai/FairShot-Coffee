package com.coffeeShop.Coffee.Shop.util;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.coffeeShop.Coffee.Shop.dto.BaristaResponse;
import com.coffeeShop.Coffee.Shop.dto.OrderRequest;
import com.coffeeShop.Coffee.Shop.dto.OrderResponse;
import com.coffeeShop.Coffee.Shop.model.Barista;
import com.coffeeShop.Coffee.Shop.model.Order;
import com.coffeeShop.Coffee.Shop.service.PriorityScoreService;

/**
 * Mapper for converting between domain models and DTOs.
 * 
 * This mapper handles the conversion of:
 * 1. OrderRequest → Order (for creating new orders)
 * 2. Order → OrderResponse (for API responses)
 * 3. Barista → BaristaResponse (for API responses)
 * 
 * The mapper ensures clean separation between API layer and domain layer.
 */
@Component
public class OrderMapper {

    private final PriorityScoreService priorityScoreService;

    @Autowired
    public OrderMapper(PriorityScoreService priorityScoreService) {
        this.priorityScoreService = priorityScoreService;
    }

    /**
     * Converts OrderRequest to Order domain model.
     * Sets initial values for new orders.
     */
    public Order toOrder(OrderRequest orderRequest) {
        return Order.builder()
                .id(UUID.randomUUID())
                .arrivalTime(Instant.now())
                .drinkType(orderRequest.getDrinkType())
                .customerType(orderRequest.getCustomerType())
                .status(com.coffeeShop.Coffee.Shop.model.OrderStatus.PENDING)
                .build();
    }

    /**
     * Converts Order domain model to OrderResponse DTO.
     * Includes calculated fields like wait time and priority score.
     */
    public OrderResponse toOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setArrivalTime(order.getArrivalTime());
        response.setDrinkType(order.getDrinkType());
        response.setCustomerType(order.getCustomerType());
        response.setStatus(order.getStatus());
        response.setStartTime(order.getStartTime());
        response.setCompletionTime(order.getCompletionTime());
        response.setAssignedBaristaId(order.getAssignedBaristaId());

        // Calculate wait time
        long waitMinutes = Duration.between(order.getArrivalTime(), Instant.now()).toMinutes();
        response.setWaitTimeMinutes(waitMinutes);

        // Check if order is overdue
        response.setOverdue(waitMinutes > Order.MAX_WAIT_MINUTES);

        // New fields from backend fixes
        response.setPriceInRupees(order.getPriceInRupees());
        response.setSkipCount(order.getSkipCount());
        response.setExplanation(order.getExplanation());
        response.setUrgent(order.isUrgent());

        return response;
    }

    /**
     * Converts a list of Orders to OrderResponse DTOs.
     */
    public List<OrderResponse> toOrderResponseList(List<Order> orders) {
        return orders.stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());
    }

    /**
     * Converts Barista domain model to BaristaResponse DTO.
     */
    public BaristaResponse toBaristaResponse(Barista barista) {
        BaristaResponse response = new BaristaResponse();
        response.setId(barista.getId());
        response.setName(barista.getName());
        
        // CRITICAL: Use barista's own availability check (single source of truth)
        response.setAvailable(barista.isAvailable());
        response.setBusy(barista.isBusy());
        
        // Set full current order (not just ID)
        if (barista.getCurrentOrder() != null) {
            response.setCurrentOrder(toOrderResponse(barista.getCurrentOrder()));
            response.setCurrentOrderId(barista.getCurrentOrder().getId());
            response.setOrderStartTime(barista.getCurrentOrder().getStartTime());
            response.setOrderEndTime(barista.getCurrentOrder().getCompletionTime());
        } else {
            response.setCurrentOrder(null);
            response.setCurrentOrderId(null);
            response.setOrderStartTime(null);
            response.setOrderEndTime(null);
        }
        
        // Utilization metrics
        response.setTotalWorkedMinutes(barista.getTotalWorkedMinutes() != null ? barista.getTotalWorkedMinutes() : 0);
        response.setRemainingMinutes(barista.getRemainingMinutes());
        response.setBusyUntil(barista.getBusyUntil());
        
        return response;
    }

    /**
     * Converts a list of Baristas to BaristaResponse DTOs.
     */
    public List<BaristaResponse> toBaristaResponseList(List<Barista> baristas) {
        return baristas.stream()
                .map(this::toBaristaResponse)
                .collect(Collectors.toList());
    }
}
