package com.barnizgallery.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/** One image of {@code POST /v1/designs}, in base64 with its own media type (never assume PNG). */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiDesignImage(String imageBase64, String mimeType, String composition, String prompt) {
}
