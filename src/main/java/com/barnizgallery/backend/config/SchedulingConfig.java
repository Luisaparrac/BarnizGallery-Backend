package com.barnizgallery.backend.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables scheduled tasks (the auction scheduler) only when
 * {@code app.auction.scheduler-enabled=true}, the default in production.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "app.auction.scheduler-enabled", havingValue = "true", matchIfMissing = true)
public class SchedulingConfig {
}
