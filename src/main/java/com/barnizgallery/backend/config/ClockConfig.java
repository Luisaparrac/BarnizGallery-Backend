package com.barnizgallery.backend.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides the {@link Clock} used by the auction logic, so unit tests can control "now".
 */
@Configuration
public class ClockConfig {

    /** UTC clock: auction dates in the database are UTC. */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
