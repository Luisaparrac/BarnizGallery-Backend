package com.barnizgallery.backend.enums;

/**
 * Shipping progress of an order line. Stored in {@code order_items.fulfillment_status}, a text column without a
 * database CHECK; these values are the backend's choice until the database defines them.
 */
public enum FulfillmentStatus implements DbValueEnum {

    PENDING("pendiente"),
    SHIPPED("enviado"),
    DELIVERED("entregado");

    private final String dbValue;

    FulfillmentStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }
}
