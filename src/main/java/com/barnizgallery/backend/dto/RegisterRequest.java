package com.barnizgallery.backend.dto;

import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.enums.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Sign-up of an account. The password needs 8 to 72 characters. The frontend sends {@code fullName} instead of
 * {@code name}, and {@code role} ({@code CUSTOMER} by default, {@code ARTISAN} with {@code workshop} and
 * {@code city}); {@code avatarId} is accepted and not used yet.
 */
public record RegisterRequest(
        @Size(max = 100) String name,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @Pattern(regexp = "[A-Z]{2}") String countryCode,
        Language preferredLanguage,
        @Size(max = 100) String fullName,
        UserRole role,
        Long avatarId,
        @Size(max = 150) String workshop,
        @Size(max = 100) String city) {

    /** The name sent as {@code name} or, as the frontend sends it, {@code fullName}. */
    public String displayName() {
        return name != null && !name.isBlank() ? name : fullName;
    }
}
