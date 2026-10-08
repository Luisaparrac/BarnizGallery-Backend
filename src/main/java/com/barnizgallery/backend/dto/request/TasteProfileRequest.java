package com.barnizgallery.backend.dto.request;

import java.util.List;

import jakarta.validation.constraints.Size;

/**
 * Answers of the initial questionnaire.
 */
public record TasteProfileRequest(
        List<String> preferredColors,
        List<String> preferredTypes,
        List<String> preferredStyles,
        @Size(max = 100) String budgetRange) {
}
