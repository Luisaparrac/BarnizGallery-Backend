package com.barnizgallery.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Body to create or update a master.
 */
public record MasterRequest(
        @NotBlank @Size(max = 100) String name,
        String biographyEs,
        String biographyEn,
        @Size(max = 150) String workshop,
        @Size(max = 200) String contactInfo) {
}
