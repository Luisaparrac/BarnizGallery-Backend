package com.barnizgallery.backend.enums;

/**
 * Kind of artisan offer for a custom request. Stored in {@code custom_offers.offer_type}.
 */
public enum OfferType implements DbValueEnum {

    ACCEPTS_PRICE("acepta_precio"),
    COUNTER_OFFER("contraoferta");

    private final String dbValue;

    OfferType(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }
}
