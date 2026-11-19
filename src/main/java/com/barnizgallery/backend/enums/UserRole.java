package com.barnizgallery.backend.enums;

/**
 * Role of a user account. Stored in {@code users.role}.
 */
public enum UserRole implements DbValueEnum {

    ADMIN("administrador"),
    ARTISAN("artesano"),
    CUSTOMER("cliente");

    private final String dbValue;

    UserRole(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }
}
