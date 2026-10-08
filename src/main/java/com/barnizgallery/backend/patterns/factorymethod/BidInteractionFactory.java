package com.barnizgallery.backend.patterns.factorymethod;

import org.springframework.stereotype.Component;

import com.barnizgallery.backend.enums.InteractionAction;

/**
 * <b>Factory Method pattern – ConcreteCreator.</b>
 * <p>
 * Creates BID interactions (weight 5, the strongest signal of interest).
 * They are recorded automatically when a bid is placed (by an auction observer).
 */
@Component
public class BidInteractionFactory extends InteractionFactory {

    @Override
    protected InteractionAction action() {
        return InteractionAction.BID;
    }

    @Override
    public int weight() {
        return 5;
    }
}
