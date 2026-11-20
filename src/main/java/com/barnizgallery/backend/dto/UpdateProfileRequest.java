package com.barnizgallery.backend.dto;

import com.barnizgallery.backend.enums.Language;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Changes of the logged-in user's profile. A null field is not changed; an empty {@code avatarCode} removes the
 * avatar. The avatar is chosen by catalog code.
 */
public record UpdateProfileRequest(
        @Size(min = 1, max = 100) String name,
        @Size(max = 40) String phone,
        @Pattern(regexp = "[A-Z]{2}") String countryCode,
        Language preferredLanguage,
        @Size(max = 60) String avatarCode,
        @Size(min = 1, max = 100) String fullName,
        Long avatarId) {
}
