package com.barnizgallery.backend.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * An artwork as {@code POST /v1/similar} knows it: id, slug of the catalog index and store.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiArtworkRef(Long artworkId, String slug, Long storeId) {
}
