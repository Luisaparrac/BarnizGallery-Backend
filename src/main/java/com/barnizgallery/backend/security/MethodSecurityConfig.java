package com.barnizgallery.backend.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Enables {@code @PreAuthorize} on services and controllers, for rules that depend on the data (for example,
 * an artisan may only edit the artworks of their own store).
 */
@Configuration
@EnableMethodSecurity
public class MethodSecurityConfig {
}
