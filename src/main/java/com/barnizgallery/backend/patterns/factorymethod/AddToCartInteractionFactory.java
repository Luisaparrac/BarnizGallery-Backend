package com.barnizgallery.backend.patterns.factorymethod;

import org.springframework.stereotype.Component;

import com.barnizgallery.backend.enums.InteractionAction;

/**
 * <b>Factory Method pattern – ConcreteCreator.</b>
 * <p>
 * Creates ADD_TO_CART interactions (weight 5): the user put the artwork in the cart, a strong sign of
 * interest even before paying.
 */
@Component
public class AddToCartInteractionFactory extends InteractionFactory {

    @Override
    protected InteractionAction action() {
        return InteractionAction.ADD_TO_CART;
    }

    @Override
    public int weight() {
        return 5;
    }
}
