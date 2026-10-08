package com.barnizgallery.backend.patterns.factorymethod;

import java.time.LocalDateTime;

import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.model.entity.Artwork;
import com.barnizgallery.backend.model.entity.Interaction;
import com.barnizgallery.backend.model.entity.Visitor;
import com.barnizgallery.backend.model.enums.InteractionAction;

/**
 * <b>Factory Method pattern – Creator (abstract).</b>
 * <p>
 * Creates {@link Interaction} objects (the Product). The common steps live in
 * {@link #create(Visitor, Artwork, Integer)}; each subclass (ConcreteCreator) decides
 * the parts that change: which action it creates ({@link #action()}, the factory method),
 * how much the interaction is worth for recommendations ({@link #weight()}) and
 * its own validation ({@link #validateDuration(Integer)}).
 * <p>
 * This way, adding a new kind of interaction only needs a new subclass;
 * the service that records interactions does not change.
 */
public abstract class InteractionFactory {

    /**
     * Builds a new interaction ready to be saved. It is final so all interactions
     * follow the same steps.
     */
    public final Interaction create(Visitor visitor, Artwork artwork, Integer durationSeconds) {
        if (visitor == null || artwork == null) {
            throw new IllegalArgumentException("Visitor and artwork are required");
        }
        if (durationSeconds != null && durationSeconds < 0) {
            throw BusinessRuleException.unprocessable("durationSeconds must be >= 0");
        }
        validateDuration(durationSeconds);

        Interaction interaction = new Interaction();
        interaction.setVisitor(visitor);
        interaction.setArtwork(artwork);
        interaction.setAction(action());
        interaction.setDurationSeconds(durationSeconds);
        interaction.setInteractionDate(LocalDateTime.now());
        return interaction;
    }

    /** The action this creator produces. This is the factory method. */
    protected abstract InteractionAction action();

    /** Weight of this kind of interaction for the recommendation algorithms. */
    public abstract int weight();

    /** Extra validation of the duration. By default the duration is optional. */
    protected void validateDuration(Integer durationSeconds) {
        // optional by default
    }

    /** Public view of {@link #action()}, used by {@link InteractionFactoryProvider} to register creators. */
    public final InteractionAction supportedAction() {
        return action();
    }
}
