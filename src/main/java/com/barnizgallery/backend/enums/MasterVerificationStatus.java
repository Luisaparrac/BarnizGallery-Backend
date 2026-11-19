package com.barnizgallery.backend.enums;

/**
 * Verification status of a master artisan. Stored in {@code masters.verification_status}.
 */
public enum MasterVerificationStatus implements DbValueEnum {

    PENDING("pendiente"),
    IN_REVIEW("en_revision"),
    APPROVED("aprobado"),
    REJECTED("rechazado");

    private final String dbValue;

    MasterVerificationStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }
}
