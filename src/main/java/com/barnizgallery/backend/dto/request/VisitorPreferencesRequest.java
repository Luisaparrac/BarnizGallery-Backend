package com.barnizgallery.backend.dto.request;

import com.barnizgallery.backend.model.enums.CameraMode;
import com.barnizgallery.backend.model.enums.Language;

/**
 * Body to change the language and/or camera mode. Null fields are not changed.
 */
public record VisitorPreferencesRequest(
        Language preferredLanguage,
        CameraMode cameraMode) {
}
