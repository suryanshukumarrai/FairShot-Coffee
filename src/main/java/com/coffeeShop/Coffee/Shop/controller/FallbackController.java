package com.coffeeShop.Coffee.Shop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Fallback controller for Single Page Application (SPA) routing.
 * 
 * Handles requests to non-API routes by forwarding them to index.html,
 * allowing React Router to handle client-side routing.
 */
@Controller
public class FallbackController {

    /**
     * Serve index.html for all React Router paths
     * Maps to all common frontend routes
     */
    @GetMapping({
        "/",
        "/order",
        "/status",
        "/status/**",
        "/public-queue",
        "/barista",
        "/manager",
        "/simulation"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
