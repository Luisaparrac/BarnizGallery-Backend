package com.barnizgallery.backend.enums;

/**
 * Status of an AI design of a custom request (CHECK in V4). Stored in {@code custom_request_designs.status}.
 */
public enum DesignStatus implements DbValueEnum {

    PENDING("pendiente"),
    COMPLETED("completado"),
    FAILED("fallido");

    private final String dbValue;

    DesignStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }
}
