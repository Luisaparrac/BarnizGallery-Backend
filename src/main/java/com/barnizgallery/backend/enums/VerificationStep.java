package com.barnizgallery.backend.enums;

/**
 * Step of the artisan verification. Stored in {@code master_verifications.step}.
 */
public enum VerificationStep implements DbValueEnum {

    EMAIL("correo"),
    IDENTITY("identidad"),
    WORKSHOP("taller"),
    SAMPLES("muestras"),
    ADMIN_APPROVAL("aprobacion_admin");

    private final String dbValue;

    VerificationStep(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }
}
