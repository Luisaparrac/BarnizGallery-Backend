package com.barnizgallery.backend.converter;

import com.barnizgallery.backend.enums.OrderType;

import jakarta.persistence.Converter;

/**
 * Converts {@link OrderType} to its Spanish database value and back.
 */
@Converter
public class OrderTypeConverter extends DbValueEnumConverter<OrderType> {

    public OrderTypeConverter() {
        super(OrderType.class);
    }
}
