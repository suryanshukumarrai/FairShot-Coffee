package com.coffeeShop.Coffee.Shop.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DrinkType {
    COLD_BREW(1, "Cold Brew", 120),
    ESPRESSO(2, "Espresso", 150),
    AMERICANO(2, "Americano", 140),
    CAPPUCCINO(4, "Cappuccino", 180),
    LATTE(4, "Latte", 200),
    MOCHA(6, "Mocha", 250);

    private final int prepTimeMinutes;
    private final String displayName;
    private final int priceInRupees;
}
