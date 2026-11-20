package com.barnizgallery.backend.dto;

import java.util.List;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * Body of {@code POST /v1/designs}: the custom request described in free text (shape and style in Spanish), the
 * language of the answer and how many images to draw (1 or 2).
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiDesignRequest(
        long requestId,
        String title,
        String description,
        String shape,
        String style,
        List<AiDesignColor> colors,
        List<String> motifs,
        String sizeNotes,
        String language,
        int count) {
}
