package com.barnizgallery.backend.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * One similar artwork: score from 0 to 1, shared traits and a short explanation (both in Spanish).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiSimilarItem(Long artworkId, String slug, Long storeId, double score, List<String> sharedTraits,
        boolean sameMaster, String explanation) {
}
