package com.barnizgallery.backend.patterns.factorymethod;

import org.springframework.stereotype.Component;

import com.barnizgallery.backend.enums.InteractionAction;

/**
 * <b>Factory Method pattern – ConcreteCreator.</b>
 * <p>
 * Creates TOUCH interactions (weight 2): the visitor selected the piece in the 3D world.
 */
@Component
public class TouchInteractionFactory extends InteractionFactory {

    @Override
    protected InteractionAction action() {
        return InteractionAction.TOUCH;
    }

    @Override
    public int weight() {
        return 2;
    }
}
