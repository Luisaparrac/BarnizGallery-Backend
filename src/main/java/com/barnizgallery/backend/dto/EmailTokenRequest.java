package com.barnizgallery.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * The one-time token of an account email link (email verification).
 */
public record EmailTokenRequest(
        @NotBlank @Size(max = 200) String token) {
}
