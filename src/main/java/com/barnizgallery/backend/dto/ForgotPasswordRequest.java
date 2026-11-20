package com.barnizgallery.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Asks for the email to choose a new password.
 */
public record ForgotPasswordRequest(
        @NotBlank @Email @Size(max = 255) String email) {
}
