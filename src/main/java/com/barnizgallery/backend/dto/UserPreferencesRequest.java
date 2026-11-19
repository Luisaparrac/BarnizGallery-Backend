package com.barnizgallery.backend.dto;

import com.barnizgallery.backend.enums.Language;

/**
 * Body to change the preferred language. A null value is not changed.
 */
public record UserPreferencesRequest(
        Language preferredLanguage) {
}
