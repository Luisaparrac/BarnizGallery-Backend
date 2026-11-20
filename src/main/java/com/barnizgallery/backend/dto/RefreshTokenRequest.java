package com.barnizgallery.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * A refresh token, to get new tokens or to log out.
 */
public record RefreshTokenRequest(
        @NotBlank @Size(max = 200) String refreshToken) {
}
