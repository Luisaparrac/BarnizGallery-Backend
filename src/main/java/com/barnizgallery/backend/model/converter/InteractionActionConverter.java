package com.barnizgallery.backend.model.converter;

import com.barnizgallery.backend.model.enums.InteractionAction;

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
