package com.coffeeShop.Coffee.Shop.controller;

import com.coffeeShop.Coffee.Shop.dto.OrderRequest;
import com.coffeeShop.Coffee.Shop.dto.OrderResponse;
import com.coffeeShop.Coffee.Shop.model.Order;
import com.coffeeShop.Coffee.Shop.service.QueueSchedulerService;
import com.coffeeShop.Coffee.Shop.util.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST controller for order management.
 * 
 * Provides endpoints for:
 * 1. Creating new orders
 * 2. Viewing current queue status
 * 3. Getting order details
 * 
 * This controller handles the main entry point for orders into the system.
 * All orders are processed through the priority queue system.
 */
@RestController
@RequestMapping("/api/orders")
@Slf4j
public class OrderController {
    
    private final QueueSchedulerService queueSchedulerService;
    private final OrderMapper orderMapper;
    
    @Autowired
    public OrderController(QueueSchedulerService queueSchedulerService,
                         OrderMapper orderMapper) {
        this.queueSchedulerService = queueSchedulerService;
        this.orderMapper = orderMapper;
    }
    
    /**
     * Creates a new order and adds it to the priority queue.
     * 
     * @param orderRequest The order details
     * @return The created order with assigned ID and initial status
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        try {
            log.info("Received new order request: {} for {} customer", 
                    orderRequest.getDrinkType().getDisplayName(),
                    orderRequest.getCustomerType().getDisplayName());
            
            // Convert request to domain model
            Order order = orderMapper.toOrder(orderRequest);
            
            // Add to priority queue
            queueSchedulerService.addOrder(order);
            
            // Convert to response DTO
            OrderResponse response = orderMapper.toOrderResponse(order);
            
            log.info("Created order {} and added to queue (size: {})", 
                    order.getId(), queueSchedulerService.getQueueSize());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error creating order: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Gets the current queue snapshot.
     * Returns all orders currently in the priority queue.
     * 
     * @return List of all pending orders
     */
    @GetMapping("/queue")
    public ResponseEntity<List<OrderResponse>> getCurrentQueue() {
        try {
            List<Order> orders = queueSchedulerService.getAllOrders();
            List<OrderResponse> response = orderMapper.toOrderResponseList(orders);
            
            log.debug("Retrieved current queue with {} orders", orders.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error retrieving queue: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Gets all orders (including completed ones if implemented).
     * For now, returns only orders in the queue.
     * 
     * @return List of all orders
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        try {
            List<Order> orders = queueSchedulerService.getAllOrders();
            List<OrderResponse> response = orderMapper.toOrderResponseList(orders);
            
            log.debug("Retrieved {} orders", orders.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error retrieving all orders: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Gets a specific order by ID.
     * 
     * @param id The order ID
     * @return The order details or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable java.util.UUID id) {
        try {
            List<Order> orders = queueSchedulerService.getAllOrders();
            
            for (Order order : orders) {
                if (order.getId().equals(id)) {
                    OrderResponse response = orderMapper.toOrderResponse(order);
                    log.debug("Retrieved order {}", id);
                    return ResponseEntity.ok(response);
                }
            }
            
            log.debug("Order {} not found in queue", id);
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("Error retrieving order {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Gets orders by status.
     * 
     * @param status The order status to filter by
     * @return List of orders with the specified status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(@PathVariable String status) {
        try {
            List<Order> orders = queueSchedulerService.getAllOrders();
            
            List<OrderResponse> response = orders.stream()
                    .filter(order -> order.getStatus().name().equalsIgnoreCase(status))
                    .map(orderMapper::toOrderResponse)
                    .toList();
            
            log.debug("Retrieved {} orders with status '{}'", response.size(), status);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error retrieving orders by status '{}': {}", status, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Gets queue statistics.
     * 
     * @return Queue statistics including size and average wait time
     */
    @GetMapping("/stats")
    public ResponseEntity<QueueStats> getQueueStats() {
        try {
            List<Order> orders = queueSchedulerService.getAllOrders();
            int queueSize = orders.size();
            
            // Calculate average wait time
            double averageWaitTime = 0;
            if (!orders.isEmpty()) {
                averageWaitTime = orders.stream()
                        .mapToLong(Order::getWaitTimeMinutes)
                        .average()
                        .orElse(0.0);
            }
            
            QueueStats stats = new QueueStats(queueSize, averageWaitTime);
            
            log.debug("Queue stats: size={}, avg_wait={:.2f} min", queueSize, averageWaitTime);
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Error retrieving queue stats: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Data class for queue statistics.
     */
    public static class QueueStats {
        public final int queueSize;
        public final double averageWaitTimeMinutes;
        
        public QueueStats(int queueSize, double averageWaitTimeMinutes) {
            this.queueSize = queueSize;
            this.averageWaitTimeMinutes = averageWaitTimeMinutes;
        }
    }
}
