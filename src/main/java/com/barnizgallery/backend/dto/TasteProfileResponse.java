package com.barnizgallery.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Taste profile of a user.
 */
public record TasteProfileResponse(
        Long profileId,
        Long userId,
        List<String> preferredColors,
        List<String> preferredTypes,
        List<String> preferredStyles,
        String budgetRange,
        LocalDateTime updatedAt) {
}
