package com.barnizgallery.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Body to create or update a room (one room per master).
 */
public record RoomRequest(
        @NotNull Integer masterId,
        @NotBlank @Size(max = 150) String nameEs,
        @NotBlank @Size(max = 150) String nameEn,
        String descriptionEs,
        String descriptionEn,
        @Size(max = 255) String scene3dUrl) {
}
