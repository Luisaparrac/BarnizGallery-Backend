package com.barnizgallery.backend.dto;

import com.barnizgallery.backend.enums.CameraMode;
import com.barnizgallery.backend.enums.Language;

/**
 * Body to change the language and/or camera mode. Null fields are not changed.
 */
public record VisitorPreferencesRequest(
        Language preferredLanguage,
        CameraMode cameraMode) {
}
