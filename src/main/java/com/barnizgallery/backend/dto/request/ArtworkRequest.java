package com.barnizgallery.backend.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Body to create or update an artwork. The status is managed by auctions.
 */
public record ArtworkRequest(
        @NotNull Integer roomId,
        @NotBlank @Size(max = 150) String titleEs,
        @NotBlank @Size(max = 150) String titleEn,
        String historyEs,
        String historyEn,
        @Size(max = 150) String technique,
        @Size(max = 100) String dimensions,
        List<String> colorTags,
        List<String> motifTags) {
}
