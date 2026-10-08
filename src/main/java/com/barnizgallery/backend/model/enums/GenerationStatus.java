package com.barnizgallery.backend.model.enums;

/**
 * Status of a 3D model generation. Stored in {@code three_d_models.generation_status}.
 */
public enum GenerationStatus implements DbValueEnum {

    PENDING("pendiente"),
    PROCESSING("procesando"),
    COMPLETED("completado"),
    FAILED("fallido");

    private final String dbValue;

    GenerationStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }
}
