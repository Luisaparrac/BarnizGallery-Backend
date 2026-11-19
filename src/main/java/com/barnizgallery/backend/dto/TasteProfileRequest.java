package com.barnizgallery.backend.dto;

import java.util.List;

/**
 * Answers of the initial questionnaire, as catalog codes. Every field is optional.
 */
public record TasteProfileRequest(
        List<String> colorCodes,
        List<String> shapeCodes,
        List<String> styleCodes,
        List<String> motifCodes,
        String budgetRangeCode) {
}
