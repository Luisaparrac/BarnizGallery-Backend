package com.barnizgallery.backend.dto;

import com.barnizgallery.backend.enums.Language;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Sign-up of a customer account. The password needs 8 to 72 characters.
 */
public record RegisterRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @Pattern(regexp = "[A-Z]{2}") String countryCode,
        Language preferredLanguage) {
}
