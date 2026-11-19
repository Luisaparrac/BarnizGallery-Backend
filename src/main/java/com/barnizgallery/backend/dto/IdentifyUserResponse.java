package com.barnizgallery.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Result of identifying a user: isNew is true when it was just created.
 */
public record IdentifyUserResponse(
        UserResponse user,
        @JsonProperty("isNew") boolean isNew) {
}
