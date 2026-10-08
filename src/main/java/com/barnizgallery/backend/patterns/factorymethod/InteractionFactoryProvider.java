package com.barnizgallery.backend.patterns.factorymethod;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.barnizgallery.backend.model.enums.InteractionAction;

/**
 * Returns the {@link InteractionFactory} (ConcreteCreator) for each {@link InteractionAction}.
 * <p>
 * Spring injects every creator; each one says which action it handles, so the
 * provider has no {@code switch}.
 */
@Component
public class InteractionFactoryProvider {

    private final Map<InteractionAction, InteractionFactory> factories = new EnumMap<>(InteractionAction.class);

    public InteractionFactoryProvider(List<InteractionFactory> creators) {
        for (InteractionFactory creator : creators) {
            factories.put(creator.supportedAction(), creator);
        }
        for (InteractionAction action : InteractionAction.values()) {
            if (!factories.containsKey(action)) {
                throw new IllegalStateException("No InteractionFactory registered for " + action);
            }
        }
    }

    public InteractionFactory forAction(InteractionAction action) {
        return factories.get(action);
    }

    /** Weight of an action for the recommendation algorithms. */
    public int weightOf(InteractionAction action) {
        return forAction(action).weight();
    }
}
