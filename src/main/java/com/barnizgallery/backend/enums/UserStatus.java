package com.barnizgallery.backend.enums;

/**
 * Status of a user account. Stored in {@code users.status}.
 */
public enum UserStatus implements DbValueEnum {

    PENDING("pendiente"),
    ACTIVE("activo"),
    SUSPENDED("suspendido"),
    DELETED("eliminado");

    private final String dbValue;

    UserStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }
}
