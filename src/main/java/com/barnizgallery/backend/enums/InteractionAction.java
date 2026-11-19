package com.barnizgallery.backend.enums;

/**
 * Kind of interaction a user has with an artwork. Stored in {@code interactions.action}.
 */
public enum InteractionAction implements DbValueEnum {

    VIEW("ver"),
    ZOOM("ampliar"),
    BID("ofertar");

    private final String dbValue;

    InteractionAction(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }
}
