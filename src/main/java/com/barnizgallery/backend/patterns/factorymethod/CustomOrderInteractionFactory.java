package com.barnizgallery.backend.patterns.factorymethod;

import org.springframework.stereotype.Component;

import com.barnizgallery.backend.enums.InteractionAction;

/**
 * <b>Factory Method pattern – ConcreteCreator.</b>
 * <p>
 * Creates CUSTOM_ORDER interactions (weight 6): the user asked a store for a custom piece. The target is the
 * store, not an artwork.
 */
@Component
public class CustomOrderInteractionFactory extends InteractionFactory {

    @Override
    protected InteractionAction action() {
        return InteractionAction.CUSTOM_ORDER;
    }

    @Override
    public int weight() {
        return 6;
    }

    @Override
    protected boolean storeLevel() {
        return true;
    }
}
