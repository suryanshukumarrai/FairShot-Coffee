package com.coffeeShop.Coffee.Shop.service;

import com.coffeeShop.Coffee.Shop.dto.BaristaResponse;

import java.util.List;
import java.util.UUID;

public interface BaristaService {
    
    BaristaResponse getBaristaById(Integer id);
    
    List<BaristaResponse> getAllBaristas();
    
    List<BaristaResponse> getAvailableBaristas();
    
    void assignOrderToBarista(Integer baristaId, UUID orderId);
    
    void completeOrder(Integer baristaId);
    
    void initializeBaristas();
}
