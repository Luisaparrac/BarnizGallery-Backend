package com.barnizgallery.backend.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Taste profile of a visitor.
 */
public record TasteProfileResponse(
        Integer profileId,
        Integer visitorId,
        List<String> preferredColors,
        List<String> preferredTypes,
        List<String> preferredStyles,
        String budgetRange,
        LocalDateTime updatedAt) {
}
