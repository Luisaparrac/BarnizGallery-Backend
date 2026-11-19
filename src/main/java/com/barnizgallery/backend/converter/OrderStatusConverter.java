package com.barnizgallery.backend.converter;

import com.barnizgallery.backend.enums.OrderStatus;

import jakarta.persistence.Converter;

/**
 * Converts {@link OrderStatus} to its Spanish database value and back.
 */
@Converter
public class OrderStatusConverter extends DbValueEnumConverter<OrderStatus> {

    public OrderStatusConverter() {
        super(OrderStatus.class);
    }
}
