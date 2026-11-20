package com.barnizgallery.backend.patterns.adapter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * The fields the backend reads from PayPal's {@code POST /v1/oauth2/token} answer.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PayPalTokenResponse(String accessToken, String tokenType, long expiresIn) {
}
