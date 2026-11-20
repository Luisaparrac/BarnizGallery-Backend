package com.barnizgallery.backend.enums;

/**
 * Status of a custom request (approved by the database team; CHECK in V4). Stored in
 * {@code custom_requests.status}.
 */
public enum CustomRequestStatus implements DbValueEnum {

    DRAFT("borrador"),
    OPEN("abierta"),
    ACCEPTED("aceptada"),
    IN_PRODUCTION("en_produccion"),
    FINISHED("terminada"),
    COMPLETED("completada"),
    CANCELLED("cancelada"),
    EXPIRED("vencida");

    private final String dbValue;

    CustomRequestStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }
}
