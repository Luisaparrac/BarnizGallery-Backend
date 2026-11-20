package com.barnizgallery.backend.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/** Answer of {@code POST /v1/designs}: who drew the images, with which model and prompt. */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiDesignResponse(String provider, String model, String prompt, List<AiDesignImage> images) {
}
