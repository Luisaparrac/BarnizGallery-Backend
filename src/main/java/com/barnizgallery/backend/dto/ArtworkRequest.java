package com.barnizgallery.backend.dto;

import java.math.BigDecimal;
import java.util.List;

import com.barnizgallery.backend.enums.SaleMode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Body to create or update an artwork of the artisan's own store. The status changes through its own endpoint
 * and through auctions and orders.
 *
 * @param currency   ISO 4217 code of the price (for example USD)
 * @param colorCodes codes of the color catalog
 * @param motifCodes codes of the motif catalog
 * @param shapeCode  code of the shape catalog, or null
 * @param styleCode  code of the style catalog, or null
 */
public record ArtworkRequest(
        @NotBlank @Size(max = 150) String titleEs,
        @NotBlank @Size(max = 150) String titleEn,
        String historyEs,
        String historyEn,
        @Size(max = 150) String technique,
        @PositiveOrZero BigDecimal widthCm,
        @PositiveOrZero BigDecimal heightCm,
        @PositiveOrZero BigDecimal depthCm,
        @PositiveOrZero BigDecimal weightG,
        SaleMode saleMode,
        @PositiveOrZero BigDecimal price,
        @Pattern(regexp = "[A-Z]{3}") String currency,
        @PositiveOrZero Integer stock,
        List<String> colorCodes,
        List<String> motifCodes,
        @Size(max = 60) String shapeCode,
        @Size(max = 60) String styleCode) {
}
