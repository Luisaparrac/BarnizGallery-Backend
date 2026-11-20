package com.barnizgallery.backend.security;

import java.time.Instant;

/**
 * A signed access token and the moment it expires.
 */
public record IssuedAccessToken(String token, Instant expiresAt) {
}
