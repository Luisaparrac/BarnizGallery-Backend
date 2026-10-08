package com.barnizgallery.backend.model.enums;

/**
 * Kind of interaction a visitor has with an artwork. Stored in {@code interactions.action}.
 */
public enum InteractionAction implements DbValueEnum {

    VIEW("ver"),
    TOUCH("tocar"),
    ROTATE("girar"),
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
