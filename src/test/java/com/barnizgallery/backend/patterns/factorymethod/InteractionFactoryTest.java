package com.barnizgallery.backend.patterns.factorymethod;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.TestFactories;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Interaction;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.InteractionAction;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.exception.BusinessRuleException;

class InteractionFactoryTest {

    private final User user = TestEntities.user(1, Language.ES);
    private final Artwork artwork = TestEntities.artwork(2, TestEntities.store(1, TestEntities.master(1)),
            ArtworkStatus.EXHIBITED);

    static Stream<Arguments> creators() {
        return Stream.of(
                Arguments.of(new ViewInteractionFactory(), InteractionAction.VIEW, 1),
                Arguments.of(new ZoomInteractionFactory(), InteractionAction.ZOOM, 3),
                Arguments.of(new BidInteractionFactory(), InteractionAction.BID, 5));
    }

    @ParameterizedTest
    @MethodSource("creators")
    void eachCreatorProducesItsActionAndWeight(InteractionFactory factory, InteractionAction action, int weight) {
        Interaction interaction = factory.create(user, artwork, 10);

        assertThat(interaction.getAction()).isEqualTo(action);
        assertThat(factory.weight()).isEqualTo(weight);
        assertThat(interaction.getUser()).isSameAs(user);
        assertThat(interaction.getArtwork()).isSameAs(artwork);
        assertThat(interaction.getDurationSeconds()).isEqualTo(10);
        assertThat(interaction.getInteractionDate()).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("creators")
    void negativeDurationIsRejectedByEveryCreator(InteractionFactory factory) {
        assertThatThrownBy(() -> factory.create(user, artwork, -1)).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void viewRequiresDuration() {
        assertThatThrownBy(() -> new ViewInteractionFactory().create(user, artwork, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("duration_required");
    }

    @Test
    void otherActionsAcceptMissingDuration() {
        assertThat(new ZoomInteractionFactory().create(user, artwork, null).getDurationSeconds()).isNull();
        assertThat(new BidInteractionFactory().create(user, artwork, null).getDurationSeconds()).isNull();
    }

    @Test
    void providerReturnsTheCreatorOfEachAction() {
        InteractionFactoryProvider provider = TestFactories.provider();

        assertThat(provider.forAction(InteractionAction.ZOOM)).isInstanceOf(ZoomInteractionFactory.class);
        assertThat(provider.weightOf(InteractionAction.BID)).isEqualTo(5);
    }

    @Test
    void providerFailsFastWhenAnActionHasNoCreator() {
        assertThatThrownBy(() -> new InteractionFactoryProvider(List.of(new ViewInteractionFactory())))
                .isInstanceOf(IllegalStateException.class);
    }
}
