package com.barnizgallery.backend.patterns.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.entity.Room;
import com.barnizgallery.backend.entity.TasteProfile;
import com.barnizgallery.backend.entity.Visitor;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.InteractionAction;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.exception.FeatureDisabledException;
import com.barnizgallery.backend.patterns.adapter.AiTextClient;
import com.barnizgallery.backend.patterns.adapter.DisabledAiClient;
import com.barnizgallery.backend.patterns.factorymethod.BidInteractionFactory;
import com.barnizgallery.backend.patterns.factorymethod.InteractionFactoryProvider;
import com.barnizgallery.backend.patterns.factorymethod.RotateInteractionFactory;
import com.barnizgallery.backend.patterns.factorymethod.TouchInteractionFactory;
import com.barnizgallery.backend.patterns.factorymethod.ViewInteractionFactory;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.InteractionRepository;
import com.barnizgallery.backend.repository.RoomInteraction;
import com.barnizgallery.backend.repository.RoomRepository;
import com.barnizgallery.backend.repository.TasteProfileRepository;

/**
 * Tests of every recommendation strategy. All the data is built here, in memory.
 */
class RecommendationStrategiesTest {

    private final TasteProfileRepository tasteProfileRepository = mock(TasteProfileRepository.class);
    private final ArtworkRepository artworkRepository = mock(ArtworkRepository.class);
    private final InteractionRepository interactionRepository = mock(InteractionRepository.class);
    private final InteractionFactoryProvider factoryProvider = new InteractionFactoryProvider(List.of(
            new ViewInteractionFactory(), new TouchInteractionFactory(), new RotateInteractionFactory(),
            new BidInteractionFactory()));

    private ProfileBasedStrategy profileStrategy;
    private InteractionBasedStrategy interactionStrategy;
    private HybridStrategy hybridStrategy;

    private final Visitor visitorEs = TestEntities.visitor(1, Language.ES);
    private final Visitor visitorEn = TestEntities.visitor(1, Language.EN);
    private final Room room1 = TestEntities.room(1, TestEntities.master(1));
    private final Room room2 = TestEntities.room(2, TestEntities.master(2));

    @BeforeEach
    void setUp() {
        profileStrategy = new ProfileBasedStrategy(tasteProfileRepository, artworkRepository);
        interactionStrategy = new InteractionBasedStrategy(interactionRepository, factoryProvider);
        hybridStrategy = new HybridStrategy(profileStrategy, interactionStrategy, interactionRepository);
        when(artworkRepository.findAllByOrderByArtworkIdAsc()).thenReturn(List.of(
                TestEntities.artwork(10, room1, ArtworkStatus.EXHIBITED, "Rojo", "verde"),
                TestEntities.artwork(11, room1, ArtworkStatus.EXHIBITED, "dorado"),
                TestEntities.artwork(20, room2, ArtworkStatus.EXHIBITED, "azul", "rojo")));
    }

    private void givenPreferredColors(String... colors) {
        TasteProfile profile = new TasteProfile();
        profile.setPreferredColors(List.of(colors));
        when(tasteProfileRepository.findByVisitorVisitorId(1)).thenReturn(Optional.of(profile));
    }

    private void givenInteractions(RoomInteraction... interactions) {
        when(interactionRepository.findRoomInteractions(1)).thenReturn(List.of(interactions));
        when(interactionRepository.countByVisitorVisitorId(1)).thenReturn((long) interactions.length);
    }

    @Test
    void profileScoresByProportionOfMatchingColors() {
        givenPreferredColors("rojo", "verde", "dorado", "negro");

        List<RecommendationResult> results = profileStrategy.recommend(visitorEs);

        assertThat(results).extracting(RecommendationResult::roomId).containsExactly(1, 2);
        assertThat(results.get(0).score()).isEqualByComparingTo("75.00");
        assertThat(results.get(1).score()).isEqualByComparingTo("25.00");
        assertThat(results.get(0).reason()).startsWith("Coincide con tus colores preferidos");
    }

    @Test
    void profileReasonFollowsVisitorLanguage() {
        givenPreferredColors("azul");

        List<RecommendationResult> results = profileStrategy.recommend(visitorEn);

        assertThat(results).singleElement().satisfies(r -> {
            assertThat(r.roomId()).isEqualTo(2);
            assertThat(r.score()).isEqualByComparingTo("100");
            assertThat(r.reason()).isEqualTo("Matches your preferred colors: azul");
        });
    }

    @Test
    void profileWithoutTasteProfileGivesNothing() {
        when(tasteProfileRepository.findByVisitorVisitorId(1)).thenReturn(Optional.empty());

        assertThat(profileStrategy.recommend(visitorEs)).isEmpty();
    }

    @Test
    void interactionsUseFactoryWeightsAndDuration() {
        // room 1: ROTATE (3) with 300 s -> 3 x 2 = 6 points; room 2: VIEW (1) without duration bonus -> 1 point
        givenInteractions(interaction(1, InteractionAction.ROTATE, 300), interaction(2, InteractionAction.VIEW, 0));

        List<RecommendationResult> results = interactionStrategy.recommend(visitorEs);

        assertThat(results).extracting(RecommendationResult::roomId).containsExactly(1, 2);
        assertThat(results.get(0).score()).isEqualByComparingTo("100");
        assertThat(results.get(1).score()).isEqualByComparingTo("16.67");
    }

    @Test
    void durationFactorGoesFromOneToTwo() {
        assertThat(InteractionBasedStrategy.durationFactor(null)).isEqualTo(1.0);
        assertThat(InteractionBasedStrategy.durationFactor(150)).isEqualTo(1.5);
        assertThat(InteractionBasedStrategy.durationFactor(10_000)).isEqualTo(2.0);
    }

    @Test
    void hybridUsesOnlyProfileWithFewInteractions() {
        givenPreferredColors("azul");
        givenInteractions(interaction(1, InteractionAction.TOUCH, null));

        List<RecommendationResult> results = hybridStrategy.recommend(visitorEs);

        assertThat(results).singleElement().satisfies(r -> {
            assertThat(r.roomId()).isEqualTo(2);
            assertThat(r.score()).isEqualByComparingTo("100");
        });
    }

    @Test
    void hybridMixesSixtyPercentInteractionsAndFortyPercentProfile() {
        givenPreferredColors("azul");
        givenInteractions(interaction(1, InteractionAction.TOUCH, null), interaction(1, InteractionAction.TOUCH, null),
                interaction(1, InteractionAction.TOUCH, null));

        List<RecommendationResult> results = hybridStrategy.recommend(visitorEs);

        // room 1: 0.6 x 100 + 0.4 x 0 = 60; room 2: 0.6 x 0 + 0.4 x 100 = 40
        assertThat(results).extracting(RecommendationResult::roomId).containsExactly(1, 2);
        assertThat(results).extracting(RecommendationResult::score)
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactly(new BigDecimal("60"), new BigDecimal("40"));
    }

    @Test
    void aiStrategyParsesValidLinesOnly() {
        String answer = """
                2|88|Great match
                99|70|Unknown room
                nonsense
                1|abc|bad score
                1|150|Clamped""";

        List<RecommendationResult> results = AiStrategy.parse(answer, Set.of(1, 2));

        assertThat(results).extracting(RecommendationResult::roomId).containsExactly(2, 1);
        assertThat(results.get(1).score()).isEqualByComparingTo("100");
    }

    @Test
    void aiStrategyRefusesToRunWhenAiIsDisabled() {
        AiStrategy aiStrategy = new AiStrategy(new DisabledAiClient(), mock(RoomRepository.class), artworkRepository,
                tasteProfileRepository);

        assertThatThrownBy(() -> aiStrategy.recommend(visitorEs)).isInstanceOf(FeatureDisabledException.class);
    }

    @Test
    void resolverPicksByNameAndDefaultsToHybrid() {
        AiTextClient ai = new DisabledAiClient();
        AiStrategy aiStrategy = new AiStrategy(ai, mock(RoomRepository.class), artworkRepository,
                tasteProfileRepository);
        RecommendationStrategyResolver resolver = new RecommendationStrategyResolver(
                List.of(profileStrategy, interactionStrategy, hybridStrategy, aiStrategy), ai);

        assertThat(resolver.resolve(null)).isSameAs(hybridStrategy);
        assertThat(resolver.resolve("PROFILE")).isSameAs(profileStrategy);
        assertThat(resolver.resolve("interactions")).isSameAs(interactionStrategy);
        assertThatThrownBy(() -> resolver.resolve("ai")).isInstanceOf(FeatureDisabledException.class);
        assertThatThrownBy(() -> resolver.resolve("magic")).isInstanceOf(BusinessRuleException.class);
    }

    private static RoomInteraction interaction(int roomId, InteractionAction action, Integer duration) {
        return new RoomInteraction() {
            @Override
            public Integer getRoomId() {
                return roomId;
            }

            @Override
            public InteractionAction getAction() {
                return action;
            }

            @Override
            public Integer getDurationSeconds() {
                return duration;
            }
        };
    }
}
