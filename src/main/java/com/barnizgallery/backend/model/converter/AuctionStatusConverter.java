package com.barnizgallery.backend.model.converter;

import com.barnizgallery.backend.model.enums.AuctionStatus;

import jakarta.persistence.Converter;

/**
 * Converts {@link AuctionStatus} to its Spanish database value and back.
 */
@Converter
public class AuctionStatusConverter extends DbValueEnumConverter<AuctionStatus> {

    public AuctionStatusConverter() {
        super(AuctionStatus.class);
    }
}
