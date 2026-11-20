package com.barnizgallery.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Change of password of the logged-in user; the new one needs 8 to 72 characters.
 */
public record ChangePasswordRequest(@NotBlank String currentPassword, @NotBlank @Size(min = 8, max = 72) String newPassword) {
}
