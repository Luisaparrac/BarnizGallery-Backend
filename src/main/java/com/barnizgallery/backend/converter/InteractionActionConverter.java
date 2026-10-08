package com.barnizgallery.backend.converter;

import com.barnizgallery.backend.enums.InteractionAction;

import jakarta.persistence.Converter;

/**
 * Converts {@link InteractionAction} to its Spanish database value and back.
 */
@Converter
public class InteractionActionConverter extends DbValueEnumConverter<InteractionAction> {

    public InteractionActionConverter() {
        super(InteractionAction.class);
    }
}
