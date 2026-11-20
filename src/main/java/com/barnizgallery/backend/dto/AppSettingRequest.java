package com.barnizgallery.backend.dto;

import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.JsonNode;

/**
 * New JSON value of a setting, for example {@code 15}, {@code 0.10}, {@code "USD"}, {@code true} or {@code null}.
 */
public record AppSettingRequest(@NotNull JsonNode value) {
}
