package com.barnizgallery.backend.dto;

import java.util.List;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * Body of {@code POST /v1/similar}: the artwork being viewed and the public artworks to compare it with.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiSimilarRequest(AiCandidate artwork, List<AiCandidate> candidates, int limit) {
}
