package com.barnizgallery.backend.dto;

import java.time.Instant;

/**
 * Tokens of a session: a short access token (Bearer) and a refresh token to renew it. The refresh token is
 * shown only here; the server keeps only its hash.
 */
public record AuthResponse(
        String tokenType,
        String accessToken,
        Instant accessTokenExpiresAt,
        String refreshToken,
        Instant refreshTokenExpiresAt,
        UserResponse user) {
}
