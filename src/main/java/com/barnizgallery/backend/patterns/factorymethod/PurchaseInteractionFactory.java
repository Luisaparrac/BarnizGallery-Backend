package com.barnizgallery.backend.patterns.factorymethod;

import org.springframework.stereotype.Component;

import com.barnizgallery.backend.enums.InteractionAction;

/**
 * <b>Factory Method pattern – ConcreteCreator.</b>
 * <p>
 * Creates PURCHASE interactions (weight 8, the strongest signal). They are recorded by the backend when a
 * payment is approved, never sent by the frontend.
 */
@Component
public class PurchaseInteractionFactory extends InteractionFactory {

    @Override
    protected InteractionAction action() {
        return InteractionAction.PURCHASE;
    }

    @Override
    public int weight() {
        return 8;
    }

    @Override
    public boolean recordedByServer() {
        return true;
    }
}
