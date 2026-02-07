package com.coffeeShop.Coffee.Shop.dto;

import com.coffeeShop.Coffee.Shop.model.CustomerType;
import com.coffeeShop.Coffee.Shop.model.DrinkType;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class OrderRequest {
    @NotNull(message = "Drink type is required")
    private DrinkType drinkType;
    
    @NotNull(message = "Customer type is required")
    private CustomerType customerType;
}
