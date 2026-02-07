package com.coffeeShop.Coffee.Shop.service.impl;

import com.coffeeShop.Coffee.Shop.dto.OrderRequest;
import com.coffeeShop.Coffee.Shop.dto.OrderResponse;
import com.coffeeShop.Coffee.Shop.model.Order;
import com.coffeeShop.Coffee.Shop.service.QueueSchedulerService;
import com.coffeeShop.Coffee.Shop.util.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of OrderService.
 * 
 * This service provides the business logic for order management
 * and delegates to the queue scheduler for actual processing.
 */
@Service
@Slf4j
public class OrderServiceImpl implements com.coffeeShop.Coffee.Shop.service.OrderService {
    
    private final QueueSchedulerService queueSchedulerService;
    private final OrderMapper orderMapper;
    
    @Autowired
    public OrderServiceImpl(QueueSchedulerService queueSchedulerService,
                         OrderMapper orderMapper) {
        this.queueSchedulerService = queueSchedulerService;
        this.orderMapper = orderMapper;
    }
    
    @Override
    public OrderResponse createOrder(OrderRequest orderRequest) {
        log.info("Creating new order: {} for {} customer", 
                orderRequest.getDrinkType().getDisplayName(),
                orderRequest.getCustomerType().getDisplayName());
        
        Order order = orderMapper.toOrder(orderRequest);
        queueSchedulerService.addOrder(order);
        
        OrderResponse response = orderMapper.toOrderResponse(order);
        log.info("Created order {} and added to queue", order.getId());
        
        return response;
    }
    
    @Override
    public OrderResponse getOrderById(UUID id) {
        List<Order> orders = queueSchedulerService.getAllOrders();
        
        for (Order order : orders) {
            if (order.getId().equals(id)) {
                return orderMapper.toOrderResponse(order);
            }
        }
        
        log.debug("Order {} not found in queue", id);
        return null;
    }
    
    @Override
    public List<OrderResponse> getAllOrders() {
        List<Order> orders = queueSchedulerService.getAllOrders();
        return orderMapper.toOrderResponseList(orders);
    }
    
    @Override
    public List<OrderResponse> getOrdersByStatus(String status) {
        List<Order> orders = queueSchedulerService.getAllOrders();
        
        return orders.stream()
                .filter(order -> order.getStatus().name().equalsIgnoreCase(status))
                .map(orderMapper::toOrderResponse)
                .toList();
    }
    
    @Override
    public Order assignOrderToBarista(UUID orderId, Integer baristaId) {
        // This is handled by BaristaAssignmentService
        log.debug("Order assignment to barista {} handled by BaristaAssignmentService", baristaId);
        return null;
    }
    
    @Override
    public void completeOrder(UUID orderId) {
        // This is handled by BaristaAssignmentService
        log.debug("Order completion handled by BaristaAssignmentService");
    }
    
    @Override
    public List<Order> getOverdueOrders() {
        List<Order> orders = queueSchedulerService.getAllOrders();
        
        return orders.stream()
                .filter(Order::isOverdue)
                .toList();
    }
}
