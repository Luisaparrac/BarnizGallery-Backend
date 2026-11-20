package com.barnizgallery.backend.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonRawValue;

/**
 * A row of {@code app_settings}: its JSON value (null when not configured yet).
 */
public record AppSettingResponse(String key, @JsonRawValue String value, String description, boolean publicSetting,
        Instant updatedAt) {
}
