package com.barnizgallery.backend.security;

import java.nio.charset.StandardCharsets;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code app.jwt.*} properties.
 *
 * @param secret        HS256 signing key ({@code JWT_SECRET}); required, at least {@value #MIN_SECRET_BYTES} bytes
 * @param accessMinutes lifetime of an access token in minutes ({@code JWT_ACCESS_MINUTES}, default 15)
 * @param refreshDays   lifetime of a refresh token in days ({@code JWT_REFRESH_DAYS}, default 7)
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, int accessMinutes, int refreshDays) {

    public static final int MIN_SECRET_BYTES = 32;

    public JwtProperties {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("JWT_SECRET must be set and have at least " + MIN_SECRET_BYTES + " bytes");
        }
        if (accessMinutes <= 0 || refreshDays <= 0) {
            throw new IllegalStateException("JWT_ACCESS_MINUTES and JWT_REFRESH_DAYS must be positive");
        }
    }

    /** The secret as bytes, for the HMAC key. */
    public byte[] secretBytes() {
        return secret.getBytes(StandardCharsets.UTF_8);
    }
}
