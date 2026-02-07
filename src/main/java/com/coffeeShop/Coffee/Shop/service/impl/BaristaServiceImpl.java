package com.coffeeShop.Coffee.Shop.service.impl;

import com.coffeeShop.Coffee.Shop.dto.BaristaResponse;
import com.coffeeShop.Coffee.Shop.model.Barista;
import com.coffeeShop.Coffee.Shop.service.BaristaAssignmentService;
import com.coffeeShop.Coffee.Shop.util.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of BaristaService.
 * 
 * This service provides the business logic for barista management
 * and delegates to the assignment service for actual operations.
 */
@Service
@Slf4j
public class BaristaServiceImpl implements com.coffeeShop.Coffee.Shop.service.BaristaService {
    
    private final BaristaAssignmentService baristaAssignmentService;
    private final OrderMapper orderMapper;
    
    @Autowired
    public BaristaServiceImpl(BaristaAssignmentService baristaAssignmentService,
                           OrderMapper orderMapper) {
        this.baristaAssignmentService = baristaAssignmentService;
        this.orderMapper = orderMapper;
    }
    
    @Override
    public BaristaResponse getBaristaById(Integer id) {
        List<Barista> baristas = baristaAssignmentService.getAllBaristas();
        
        for (Barista barista : baristas) {
            if (barista.getId().equals(id)) {
                return orderMapper.toBaristaResponse(barista);
            }
        }
        
        log.debug("Barista {} not found", id);
        return null;
    }
    
    @Override
    public List<BaristaResponse> getAllBaristas() {
        List<Barista> baristas = baristaAssignmentService.getAllBaristas();
        return orderMapper.toBaristaResponseList(baristas);
    }
    
    @Override
    public List<BaristaResponse> getAvailableBaristas() {
        List<Barista> allBaristas = baristaAssignmentService.getAllBaristas();
        
        return allBaristas.stream()
                .filter(Barista::isAvailable)
                .map(orderMapper::toBaristaResponse)
                .toList();
    }
    
    @Override
    public void assignOrderToBarista(Integer baristaId, UUID orderId) {
        // This is handled automatically by BaristaAssignmentService
        log.debug("Order assignment to barista {} handled automatically by BaristaAssignmentService", baristaId);
    }
    
    @Override
    public void completeOrder(Integer baristaId) {
        // This is handled automatically by BaristaAssignmentService
        log.debug("Order completion for barista {} handled automatically by BaristaAssignmentService", baristaId);
    }
    
    @Override
    public void initializeBaristas() {
        // This is handled automatically by BaristaAssignmentService @PostConstruct
        log.debug("Barista initialization handled automatically by BaristaAssignmentService");
    }
}
