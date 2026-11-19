package com.barnizgallery.backend.enums;

/**
 * Status of one verification step. Stored in {@code master_verifications.status}.
 */
public enum VerificationStepStatus implements DbValueEnum {

    PENDING("pendiente"),
    SUBMITTED("enviado"),
    IN_REVIEW("en_revision"),
    APPROVED("aprobado"),
    REJECTED("rechazado");

    private final String dbValue;

    VerificationStepStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }
}
