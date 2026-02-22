package com.coffeeShop.Coffee.Shop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration for the Coffee Shop application.
 * 
 * Configures:
 * 1. CORS for frontend integration (development and production)
 * 2. Static resource serving (React frontend built files)
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "http://localhost:3000", // React default port
                        "http://localhost:5173", // Vite default port
                        "http://localhost:4173", // Vite preview port
                        "https://*.onrender.com" // Render production domain
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // Cache preflight requests for 1 hour
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve React static files from classpath (built frontend dist)
        // Cache static assets for 1 hour
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/public/")
                .setCachePeriod(3600);
    }
}
