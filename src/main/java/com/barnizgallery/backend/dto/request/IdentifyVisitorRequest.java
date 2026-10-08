package com.barnizgallery.backend.dto.request;

import com.barnizgallery.backend.model.enums.CameraMode;
import com.barnizgallery.backend.model.enums.Language;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Body to identify a visitor by email (created if it does not exist).
 */
public record IdentifyVisitorRequest(
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(max = 100) String name,
        @Size(max = 100) String country,
        @NotNull Language preferredLanguage,
        @NotNull CameraMode cameraMode) {
}
