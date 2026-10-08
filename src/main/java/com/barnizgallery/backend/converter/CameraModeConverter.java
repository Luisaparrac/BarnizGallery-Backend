package com.barnizgallery.backend.converter;

import com.barnizgallery.backend.enums.CameraMode;

import jakarta.persistence.Converter;

/**
 * Converts {@link CameraMode} to its Spanish database value and back.
 */
@Converter
public class CameraModeConverter extends DbValueEnumConverter<CameraMode> {

    public CameraModeConverter() {
        super(CameraMode.class);
    }
}
