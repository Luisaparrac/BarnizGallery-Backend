package com.barnizgallery.backend.enums;

/**
 * Purpose of a stored token hash. Stored in {@code auth_tokens.purpose}.
 */
public enum AuthTokenPurpose implements DbValueEnum {

    EMAIL_VERIFICATION("verificar_correo"),
    PASSWORD_RESET("recuperar_clave"),
    REFRESH("refresco");

    private final String dbValue;

    AuthTokenPurpose(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }
}
