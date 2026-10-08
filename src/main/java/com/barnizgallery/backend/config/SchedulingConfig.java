package com.barnizgallery.backend.config;

import java.time.Clock;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Time and scheduling configuration.
 * <p>
 * The {@link Clock} bean lets the auction logic ask "what time is it?" in a way that
 * unit tests can control. Scheduling is enabled only when
 * {@code app.auction.scheduler-enabled=true} (the default in production).
 */
@Configuration
public class SchedulingConfig {

    /** UTC clock: auction dates in the database are UTC. */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Configuration
    @EnableScheduling
    @ConditionalOnProperty(name = "app.auction.scheduler-enabled", havingValue = "true", matchIfMissing = true)
    static class EnabledScheduling {
    }
}
