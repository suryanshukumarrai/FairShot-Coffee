package com.coffeeShop.Coffee.Shop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration for the Coffee Shop application.
 * 
 * CRITICAL FIX #10: Add CORS configuration for frontend integration.
 * 
 * This allows the React/Vite frontend to call backend APIs during development.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "http://localhost:3000", // React default port
                        "http://localhost:5173", // Vite default port
                        "http://localhost:4173" // Vite preview port
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // Cache preflight requests for 1 hour
    }
}
