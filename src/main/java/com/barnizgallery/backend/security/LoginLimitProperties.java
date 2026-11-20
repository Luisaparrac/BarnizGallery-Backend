package com.barnizgallery.backend.security;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code app.security.login.*} properties.
 *
 * @param maxAttempts failed logins allowed for one email and IP inside the window
 * @param window      length of the window; after it the failures are forgotten
 */
@ConfigurationProperties(prefix = "app.security.login")
public record LoginLimitProperties(int maxAttempts, Duration window) {
}
