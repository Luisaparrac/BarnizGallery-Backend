package com.barnizgallery.backend.patterns.factorymethod;

import java.time.LocalDateTime;

import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Interaction;
import com.barnizgallery.backend.entity.Store;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.enums.InteractionAction;
import com.barnizgallery.backend.exception.BusinessRuleException;

/**
 * <b>Factory Method pattern – Creator (abstract).</b>
 * <p>
 * Creates {@link Interaction} objects (the Product). The common steps live in
 * {@link #create(User, Artwork, Integer)} and {@link #createForStore(User, Store, Integer)}; each subclass (ConcreteCreator) decides
 * the parts that change: which action it creates ({@link #action()}, the factory method),
 * how much the interaction is worth for recommendations ({@link #weight()}) and
 * its own validation ({@link #validateDuration(Integer)}).
 * <p>
 * This way, adding a new kind of interaction only needs a new subclass;
 * the service that records interactions does not change.
 */
public abstract class InteractionFactory {

    /**
     * Builds a new interaction with an artwork, ready to be saved. It is final so all interactions follow the
     * same steps.
     */
    public final Interaction create(User user, Artwork artwork, Integer durationSeconds) {
        if (storeLevel() || artwork == null) {
            throw BusinessRuleException.unprocessable(ErrorCode.INTERACTION_TARGET_INVALID, action().getDbValue());
        }
        return build(user, artwork, null, durationSeconds);
    }

    /** Builds a new interaction with a store (for example a store visit), ready to be saved. */
    public final Interaction createForStore(User user, Store store, Integer durationSeconds) {
        if (!storeLevel() || store == null) {
            throw BusinessRuleException.unprocessable(ErrorCode.INTERACTION_TARGET_INVALID, action().getDbValue());
        }
        return build(user, null, store, durationSeconds);
    }

    private Interaction build(User user, Artwork artwork, Store store, Integer durationSeconds) {
        if (user == null) {
            throw new IllegalArgumentException("The user is required");
        }
        if (durationSeconds != null && durationSeconds < 0) {
            throw BusinessRuleException.unprocessable(ErrorCode.DURATION_NEGATIVE);
        }
        validateDuration(durationSeconds);

        Interaction interaction = new Interaction();
        interaction.setUser(user);
        interaction.setArtwork(artwork);
        interaction.setStore(store);
        interaction.setAction(action());
        interaction.setDurationSeconds(durationSeconds);
        interaction.setInteractionDate(LocalDateTime.now());
        return interaction;
    }

    /** True when the interaction is with a store instead of an artwork. By default it is with an artwork. */
    protected boolean storeLevel() {
        return false;
    }

    /** Public view of {@link #storeLevel()}. */
    public final boolean isStoreLevel() {
        return storeLevel();
    }

    /**
     * True when only the backend records this interaction (bids, purchases); the frontend cannot send it.
     */
    public boolean recordedByServer() {
        return false;
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
