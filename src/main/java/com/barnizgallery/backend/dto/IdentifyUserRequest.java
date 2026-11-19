package com.barnizgallery.backend.dto;

import com.barnizgallery.backend.enums.Language;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Body to identify a user by email (created if it does not exist).
 *
 * @param countryCode ISO 3166-1 alpha-2 country code (optional)
 */
public record IdentifyUserRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 100) String name,
        @Pattern(regexp = "[A-Z]{2}") String countryCode,
        @NotNull Language preferredLanguage) {
}
