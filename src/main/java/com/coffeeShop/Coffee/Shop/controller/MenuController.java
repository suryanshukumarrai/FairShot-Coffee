package com.coffeeShop.Coffee.Shop.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coffeeShop.Coffee.Shop.dto.MenuItemResponse;
import com.coffeeShop.Coffee.Shop.model.DrinkType;

import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for menu operations.
 * 
 * Provides the single source of truth for drink information:
 * - Drink types available
 * - Prices in INR
 * - Preparation times
 * - Complexity levels
 * 
 * This ensures frontend never hardcodes prices or drink metadata.
 */
@RestController
@RequestMapping("/api/menu")
@CrossOrigin
@Slf4j
public class MenuController {

    /**
     * Gets all available drinks with their metadata.
     * 
     * This is the ONLY source of truth for:
     * - Drink names and display names
     * - Preparation times
     * - Prices in INR (₹)
     * - Complexity levels
     * 
     * Frontend must fetch this on load and never hardcode these values.
     * 
     * @return List of all menu items with complete metadata
     */
    @GetMapping
    public ResponseEntity<List<MenuItemResponse>> getMenu() {
        try {
            List<MenuItemResponse> menu = Arrays.stream(DrinkType.values())
                    .map(MenuItemResponse::fromDrinkType)
                    .collect(Collectors.toList());

            log.debug("Retrieved menu with {} items", menu.size());

            return ResponseEntity.ok(menu);

        } catch (Exception e) {
            log.error("Error retrieving menu: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
