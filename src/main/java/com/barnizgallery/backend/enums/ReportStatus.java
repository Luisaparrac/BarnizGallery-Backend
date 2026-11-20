package com.barnizgallery.backend.enums;

/**
 * Status of a content report. Stored in {@code content_reports.status}, a text column without a database CHECK;
 * these values are the backend's choice until the database defines them.
 */
public enum ReportStatus implements DbValueEnum {

    PENDING("pendiente"),
    RESOLVED("resuelto"),
    DISMISSED("descartado");

    private final String dbValue;

    ReportStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }
}
