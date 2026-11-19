package com.barnizgallery.backend.enums;

/**
 * Status of an artisan offer. Stored in {@code custom_offers.status}.
 */
public enum OfferStatus implements DbValueEnum {

    ACTIVE("activa"),
    ACCEPTED("aceptada"),
    REJECTED("rechazada"),
    EXPIRED("expirada");

    private final String dbValue;

    OfferStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }
}
