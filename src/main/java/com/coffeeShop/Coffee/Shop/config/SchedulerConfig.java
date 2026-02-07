package com.coffeeShop.Coffee.Shop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Configuration for Spring-managed task scheduling.
 * 
 * This provides a production-safe TaskScheduler for order completion timing.
 * Unlike raw ScheduledExecutorService, Spring manages the lifecycle properly.
 */
@Configuration
public class SchedulerConfig {

    @Bean
    public TaskScheduler orderCompletionScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        // Pool size should accommodate all baristas completing simultaneously
        scheduler.setPoolSize(10);

        // Clear thread naming for debugging
        scheduler.setThreadNamePrefix("order-completion-");

        // Ensure proper initialization
        scheduler.initialize();

        return scheduler;
    }
}
