package com.barnizgallery.backend.patterns.adapter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * The fields the backend reads from a Cloudinary upload answer.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CloudinaryUploadResponse(String publicId, String secureUrl, Integer width, Integer height) {
}
