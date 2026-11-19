package com.barnizgallery.backend.enums;

/**
 * How an artwork is offered. Stored in {@code artworks.sale_mode}.
 */
public enum SaleMode implements DbValueEnum {

    EXHIBITION("exhibicion"),
    SALE("venta"),
    AUCTION("subasta");

    private final String dbValue;

    SaleMode(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }
}
