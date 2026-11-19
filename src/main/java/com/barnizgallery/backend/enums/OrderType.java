package com.barnizgallery.backend.enums;

/**
 * Origin of an order. Stored in {@code orders.order_type}.
 */
public enum OrderType implements DbValueEnum {

    PURCHASE("compra"),
    AUCTION("subasta"),
    CUSTOM("encargo");

    private final String dbValue;

    OrderType(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }
}
