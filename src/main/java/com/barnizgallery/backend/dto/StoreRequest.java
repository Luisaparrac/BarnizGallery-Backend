package com.barnizgallery.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Body to create or update a store (one store per master).
 */
public record StoreRequest(
        @NotNull Long masterId,
        @NotNull Long templateId,
        @NotBlank @Size(max = 150) String nameEs,
        @NotBlank @Size(max = 150) String nameEn,
        String descriptionEs,
        String descriptionEn) {
}
