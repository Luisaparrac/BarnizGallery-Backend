package com.barnizgallery.backend.enums;

/**
 * Camera mode chosen by a user. Stored in {@code users.camera_mode}.
 */
public enum CameraMode implements DbValueEnum {

    FIRST_PERSON("primera_persona"),
    THIRD_PERSON("tercera_persona");

    private final String dbValue;

    CameraMode(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }
}
