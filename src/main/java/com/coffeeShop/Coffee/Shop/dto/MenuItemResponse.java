package com.coffeeShop.Coffee.Shop.dto;

import com.coffeeShop.Coffee.Shop.model.DrinkType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for menu item information.
 * 
 * Provides drink metadata for the customer order page:
 * - Drink type and display name
 * - Preparation time
 * - Price in INR
 * - Complexity level
 * 
 * This is the single source of truth for drink information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemResponse {
    private String type;              // Enum name (e.g., "ESPRESSO")
    private String displayName;       // Human-readable name (e.g., "Espresso")
    private int prepTimeMinutes;      // Preparation time
    private int priceInRupees;        // Price in INR (₹)
    private String complexity;        // "QUICK" / "MEDIUM" / "COMPLEX"

    /**
     * Creates a MenuItemResponse from a DrinkType enum.
     */
    public static MenuItemResponse fromDrinkType(DrinkType drinkType) {
        String complexity;
        if (drinkType.getPrepTimeMinutes() <= 2) {
            complexity = "QUICK";
        } else if (drinkType.getPrepTimeMinutes() <= 4) {
            complexity = "MEDIUM";
        } else {
            complexity = "COMPLEX";
        }

        return MenuItemResponse.builder()
                .type(drinkType.name())
                .displayName(drinkType.getDisplayName())
                .prepTimeMinutes(drinkType.getPrepTimeMinutes())
                .priceInRupees(drinkType.getPriceInRupees())
                .complexity(complexity)
                .build();
    }
}
