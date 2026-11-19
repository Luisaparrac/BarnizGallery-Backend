package com.barnizgallery.backend.patterns.factorymethod;

import org.springframework.stereotype.Component;

import com.barnizgallery.backend.enums.InteractionAction;

/**
 * <b>Factory Method pattern – ConcreteCreator.</b>
 * <p>
 * Creates ZOOM interactions (weight 3): the user enlarged the photos of the piece to inspect it.
 * Historical TOUCH and ROTATE interactions were migrated to this action.
 */
@Component
public class ZoomInteractionFactory extends InteractionFactory {

    @Override
    protected InteractionAction action() {
        return InteractionAction.ZOOM;
    }

    @Override
    public int weight() {
        return 3;
    }
}
