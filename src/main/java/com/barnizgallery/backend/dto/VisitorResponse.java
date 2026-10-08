package com.barnizgallery.backend.dto;

import com.barnizgallery.backend.enums.CameraMode;
import com.barnizgallery.backend.enums.Language;

/**
 * A visitor.
 */
public record VisitorResponse(
        Integer visitorId,
        String name,
        String email,
        String country,
        Language preferredLanguage,
        CameraMode cameraMode) {
}
