package com.barnizgallery.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Evidence of a verification step given as a link (for example a document already stored elsewhere).
 */
public record EvidenceRequest(
        @NotBlank @Size(max = 500) @Pattern(regexp = "https://\\S+") String evidenceUrl) {
}
