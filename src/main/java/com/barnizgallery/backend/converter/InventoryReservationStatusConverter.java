package com.barnizgallery.backend.converter;

import com.barnizgallery.backend.enums.InventoryReservationStatus;

import jakarta.persistence.Converter;

/**
 * Converts {@link InventoryReservationStatus} to its Spanish database value and back.
 */
@Converter
public class InventoryReservationStatusConverter extends DbValueEnumConverter<InventoryReservationStatus> {

    public InventoryReservationStatusConverter() {
        super(InventoryReservationStatus.class);
    }
}
