package com.barnizgallery.backend.patterns.factorymethod;

import org.springframework.stereotype.Component;

import com.barnizgallery.backend.enums.InteractionAction;

/**
 * <b>Factory Method pattern – ConcreteCreator.</b>
 * <p>
 * Creates FAVORITE interactions (weight 4): the user saved the artwork among their favorites.
 */
@Component
public class FavoriteInteractionFactory extends InteractionFactory {

    @Override
    protected InteractionAction action() {
        return InteractionAction.FAVORITE;
    }

    @Override
    public int weight() {
        return 4;
    }
}
