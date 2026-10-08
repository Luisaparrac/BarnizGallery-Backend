package com.barnizgallery.backend.enums;

/**
 * Preferred language of a visitor. Stored in {@code visitors.preferred_language}.
 */
public enum Language implements DbValueEnum {

    ES("es"),
    EN("en");

    private final String dbValue;

    Language(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }
}
