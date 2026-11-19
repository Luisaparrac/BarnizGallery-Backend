package com.barnizgallery.backend.enums;

/**
 * Publication status of a store. Stored in {@code stores.status}.
 */
public enum StoreStatus implements DbValueEnum {

    DRAFT("borrador"),
    PUBLISHED("publicada"),
    SUSPENDED("suspendida");

    private final String dbValue;

    StoreStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }
}
