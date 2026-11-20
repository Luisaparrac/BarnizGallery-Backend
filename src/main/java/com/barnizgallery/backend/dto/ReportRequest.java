package com.barnizgallery.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * A report of content: {@code entityType} is ARTWORK, STORE, MESSAGE, CUSTOM_REQUEST or USER.
 */
public record ReportRequest(
        @NotBlank @Pattern(regexp = "ARTWORK|STORE|MESSAGE|CUSTOM_REQUEST|USER") String entityType,
        @NotNull Long entityId,
        @NotBlank @Size(max = 1000) String reason) {
}
