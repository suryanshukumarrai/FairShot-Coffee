package com.coffeeShop.Coffee.Shop.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CustomerType {
    REGULAR("Regular"),
    NEW("New"),
    GOLD("Gold");

    private final String displayName;
}
