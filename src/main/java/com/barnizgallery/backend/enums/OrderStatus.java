package com.barnizgallery.backend.enums;

/**
 * Status of an order. Stored in {@code orders.status}.
 */
public enum OrderStatus implements DbValueEnum {

    PENDING_PAYMENT("pendiente_pago"),
    PAID("pagado"),
    PREPARING("en_preparacion"),
    SHIPPED("enviado"),
    DELIVERED("entregado"),
    CANCELLED("cancelado"),
    REFUNDED("reembolsado");

    private final String dbValue;

    OrderStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }
}
