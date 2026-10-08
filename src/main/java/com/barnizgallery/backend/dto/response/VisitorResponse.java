package com.barnizgallery.backend.dto.response;

import com.barnizgallery.backend.model.enums.CameraMode;
import com.barnizgallery.backend.model.enums.Language;

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
