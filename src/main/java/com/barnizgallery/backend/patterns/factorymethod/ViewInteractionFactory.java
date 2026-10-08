package com.barnizgallery.backend.patterns.factorymethod;

import org.springframework.stereotype.Component;

import com.barnizgallery.backend.enums.InteractionAction;
import com.barnizgallery.backend.exception.BusinessRuleException;

/**
 * <b>Factory Method pattern – ConcreteCreator.</b>
 * <p>
 * Creates VIEW interactions (weight 1). Viewing is only meaningful with a duration,
 * so here {@code durationSeconds} is required.
 */
@Component
public class ViewInteractionFactory extends InteractionFactory {

    @Override
    protected InteractionAction action() {
        return InteractionAction.VIEW;
    }

    @Override
    public int weight() {
        return 1;
    }

    @Override
    protected void validateDuration(Integer durationSeconds) {
        if (durationSeconds == null) {
            throw BusinessRuleException.unprocessable("durationSeconds is required for VIEW interactions");
        }
    }
}
