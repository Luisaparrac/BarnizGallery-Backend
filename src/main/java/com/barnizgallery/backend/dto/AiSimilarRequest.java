package com.barnizgallery.backend.dto;

import java.util.List;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * Body of {@code POST /v1/similar} (AI service 1.2): the artwork being viewed and the public artworks that can be
 * shown (at most 500), each with its slug; {@code limit} from 1 to 50.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiSimilarRequest(AiArtworkRef artwork, List<AiArtworkRef> candidates, int limit) {
}
