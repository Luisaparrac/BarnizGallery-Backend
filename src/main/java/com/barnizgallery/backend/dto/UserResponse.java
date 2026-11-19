package com.barnizgallery.backend.dto;

import com.barnizgallery.backend.enums.CameraMode;
import com.barnizgallery.backend.enums.Language;

/**
 * A user.
 */
public record UserResponse(
        Long userId,
        String name,
        String email,
        String country,
        Language preferredLanguage,
        CameraMode cameraMode) {
}
