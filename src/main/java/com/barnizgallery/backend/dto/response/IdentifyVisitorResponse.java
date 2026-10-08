package com.barnizgallery.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Result of identifying a visitor: isNew is true when it was just created.
 */
public record IdentifyVisitorResponse(
        VisitorResponse visitor,
        @JsonProperty("isNew") boolean isNew) {
}
