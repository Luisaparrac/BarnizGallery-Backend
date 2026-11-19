package com.barnizgallery.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Taste profile of a user, as catalog codes.
 */
public record TasteProfileResponse(
        Long profileId,
        Long userId,
        List<String> colorCodes,
        List<String> shapeCodes,
        List<String> styleCodes,
        List<String> motifCodes,
        String budgetRangeCode,
        LocalDateTime updatedAt) {
}
