package com.barnizgallery.backend.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * A custom request as the frontend sends it ({@code CustomRequestInput}): catalog values as ids. The database needs a
 * shape, a style and an offered price; {@code desiredDate} is an ISO date ({@code 2026-12-24}) or date-time.
 */
public record CustomRequestInput(
        @NotBlank @Size(max = 150) String title,
        @Size(max = 4000) String description,
        Long shapeId,
        Long styleId,
        List<Long> colorIds,
        List<Long> motifIds,
        @Size(max = 1000) String sizeNotes,
        @PositiveOrZero BigDecimal offeredPrice,
        String desiredDate,
        Long targetMasterId) {
}
