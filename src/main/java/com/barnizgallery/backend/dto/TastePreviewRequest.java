package com.barnizgallery.backend.dto;

import java.util.List;

/**
 * Answers of the visitor's taste kiosk, as catalog ids (as the frontend keeps them). Nothing is saved.
 */
public record TastePreviewRequest(List<Long> colorIds, List<Long> shapeIds, List<Long> styleIds, List<Long> motifIds,
        Long budgetRangeId) {
}
