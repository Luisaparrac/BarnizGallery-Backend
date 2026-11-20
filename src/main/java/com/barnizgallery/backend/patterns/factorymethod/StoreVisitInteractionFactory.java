package com.barnizgallery.backend.patterns.factorymethod;

import org.springframework.stereotype.Component;

import com.barnizgallery.backend.enums.InteractionAction;

/**
 * <b>Factory Method pattern – ConcreteCreator.</b>
 * <p>
 * Creates STORE_VISIT interactions (weight 2): the user opened the page of a store. The target is the store,
 * not an artwork.
 */
@Component
public class StoreVisitInteractionFactory extends InteractionFactory {

    @Override
    protected InteractionAction action() {
        return InteractionAction.STORE_VISIT;
    }

    @Override
    public int weight() {
        return 2;
    }

    @Override
    protected boolean storeLevel() {
        return true;
    }
}
