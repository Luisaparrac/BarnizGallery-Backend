package com.barnizgallery.backend.dto;

import java.time.Instant;

import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.enums.UserRole;
import com.barnizgallery.backend.enums.UserStatus;

/**
 * A user account (never includes the password hash).
 */
public record UserResponse(
        Long userId,
        String name,
        String email,
        UserRole role,
        UserStatus status,
        String countryCode,
        Language preferredLanguage,
        String avatarCode,
        Long id,
        String fullName,
        Long avatarId,
        String phone,
        boolean emailVerified,
        Instant createdAt,
        Instant lastLoginAt) {
}
