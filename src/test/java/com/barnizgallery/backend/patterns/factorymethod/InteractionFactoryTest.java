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
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.model.entity.Artwork;
import com.barnizgallery.backend.model.entity.Interaction;
import com.barnizgallery.backend.model.entity.Visitor;
import com.barnizgallery.backend.model.enums.ArtworkStatus;
import com.barnizgallery.backend.model.enums.InteractionAction;
import com.barnizgallery.backend.model.enums.Language;

class InteractionFactoryTest {

    private final Visitor visitor = TestEntities.visitor(1, Language.ES);
    private final Artwork artwork = TestEntities.artwork(2, TestEntities.room(1, TestEntities.master(1)),
            ArtworkStatus.EXHIBITED);

    static Stream<Arguments> creators() {
        return Stream.of(
                Arguments.of(new ViewInteractionFactory(), InteractionAction.VIEW, 1),
                Arguments.of(new TouchInteractionFactory(), InteractionAction.TOUCH, 2),
                Arguments.of(new RotateInteractionFactory(), InteractionAction.ROTATE, 3),
                Arguments.of(new BidInteractionFactory(), InteractionAction.BID, 5));
    }

    @ParameterizedTest
    @MethodSource("creators")
    void eachCreatorProducesItsActionAndWeight(InteractionFactory factory, InteractionAction action, int weight) {
        Interaction interaction = factory.create(visitor, artwork, 10);

        assertThat(interaction.getAction()).isEqualTo(action);
        assertThat(factory.weight()).isEqualTo(weight);
        assertThat(interaction.getVisitor()).isSameAs(visitor);
        assertThat(interaction.getArtwork()).isSameAs(artwork);
        assertThat(interaction.getDurationSeconds()).isEqualTo(10);
        assertThat(interaction.getInteractionDate()).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("creators")
    void negativeDurationIsRejectedByEveryCreator(InteractionFactory factory) {
        assertThatThrownBy(() -> factory.create(visitor, artwork, -1)).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void viewRequiresDuration() {
        assertThatThrownBy(() -> new ViewInteractionFactory().create(visitor, artwork, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("durationSeconds is required");
    }

    @Test
    void otherActionsAcceptMissingDuration() {
        assertThat(new TouchInteractionFactory().create(visitor, artwork, null).getDurationSeconds()).isNull();
        assertThat(new RotateInteractionFactory().create(visitor, artwork, null).getDurationSeconds()).isNull();
        assertThat(new BidInteractionFactory().create(visitor, artwork, null).getDurationSeconds()).isNull();
    }

    @Test
    void providerReturnsTheCreatorOfEachAction() {
        InteractionFactoryProvider provider = new InteractionFactoryProvider(List.of(new ViewInteractionFactory(),
                new TouchInteractionFactory(), new RotateInteractionFactory(), new BidInteractionFactory()));

        assertThat(provider.forAction(InteractionAction.ROTATE)).isInstanceOf(RotateInteractionFactory.class);
        assertThat(provider.weightOf(InteractionAction.BID)).isEqualTo(5);
    }

    @Test
    void providerFailsFastWhenAnActionHasNoCreator() {
        assertThatThrownBy(() -> new InteractionFactoryProvider(List.of(new ViewInteractionFactory())))
                .isInstanceOf(IllegalStateException.class);
    }
}
