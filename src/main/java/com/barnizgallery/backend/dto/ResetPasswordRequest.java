package com.barnizgallery.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Chooses a new password with the token of the email link.
 */
public record ResetPasswordRequest(
        @NotBlank @Size(max = 200) String token,
        @NotBlank @Size(min = 8, max = 72) String newPassword) {
}
