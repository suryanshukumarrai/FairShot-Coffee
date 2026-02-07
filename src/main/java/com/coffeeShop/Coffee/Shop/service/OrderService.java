package com.coffeeShop.Coffee.Shop.service;

import com.coffeeShop.Coffee.Shop.dto.OrderRequest;
import com.coffeeShop.Coffee.Shop.dto.OrderResponse;
import com.coffeeShop.Coffee.Shop.model.Order;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    
    OrderResponse createOrder(OrderRequest orderRequest);
    
    OrderResponse getOrderById(UUID id);
    
    List<OrderResponse> getAllOrders();
    
    List<OrderResponse> getOrdersByStatus(String status);
    
    Order assignOrderToBarista(UUID orderId, Integer baristaId);
    
    void completeOrder(UUID orderId);
    
    List<Order> getOverdueOrders();
}
