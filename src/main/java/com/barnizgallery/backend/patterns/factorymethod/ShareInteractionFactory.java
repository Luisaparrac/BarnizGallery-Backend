package com.barnizgallery.backend.patterns.factorymethod;

import org.springframework.stereotype.Component;

import com.barnizgallery.backend.enums.InteractionAction;

/**
 * <b>Factory Method pattern – ConcreteCreator.</b>
 * <p>
 * Creates SHARE interactions (weight 3): the user shared the artwork with someone else.
 */
@Component
public class ShareInteractionFactory extends InteractionFactory {

    @Override
    protected InteractionAction action() {
        return InteractionAction.SHARE;
    }

    @Override
    public int weight() {
        return 3;
    }
}
