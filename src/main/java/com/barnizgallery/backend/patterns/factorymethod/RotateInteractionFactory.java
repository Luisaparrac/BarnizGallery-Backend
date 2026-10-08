package com.barnizgallery.backend.patterns.factorymethod;

import org.springframework.stereotype.Component;

import com.barnizgallery.backend.enums.InteractionAction;

/**
 * <b>Factory Method pattern – ConcreteCreator.</b>
 * <p>
 * Creates ROTATE interactions (weight 3): the visitor rotated the 3D model to inspect it.
 */
@Component
public class RotateInteractionFactory extends InteractionFactory {

    @Override
    protected InteractionAction action() {
        return InteractionAction.ROTATE;
    }

    @Override
    public int weight() {
        return 3;
    }
}
