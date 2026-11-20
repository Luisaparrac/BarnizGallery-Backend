package com.barnizgallery.backend.patterns.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.TestFactories;
import com.barnizgallery.backend.entity.Store;
import com.barnizgallery.backend.entity.TasteProfile;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.InteractionAction;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.exception.FeatureDisabledException;
import com.barnizgallery.backend.patterns.adapter.AiTextClient;
import com.barnizgallery.backend.patterns.adapter.DisabledAiClient;
import com.barnizgallery.backend.patterns.factorymethod.InteractionFactoryProvider;
import com.barnizgallery.backend.repository.ArtworkCatalogViewRepository;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.InteractionRepository;
import com.barnizgallery.backend.repository.StoreInteraction;
import com.barnizgallery.backend.repository.TasteProfileRepository;

/**
 * Tests of every recommendation strategy. All the data is built here, in memory.
 */
class RecommendationStrategiesTest {

    private final TasteProfileRepository tasteProfileRepository = mock(TasteProfileRepository.class);
    private final ArtworkRepository artworkRepository = mock(ArtworkRepository.class);
    private final InteractionRepository interactionRepository = mock(InteractionRepository.class);
    private final InteractionFactoryProvider factoryProvider = TestFactories.provider();
    private final ArtworkCatalogViewRepository catalogRepository = mock(ArtworkCatalogViewRepository.class);
    private final PublicArtworkCatalog publicCatalog = new PublicArtworkCatalog(artworkRepository, catalogRepository);

    private ProfileBasedStrategy profileStrategy;
    private InteractionBasedStrategy interactionStrategy;
    private HybridStrategy hybridStrategy;

    private final User userEs = TestEntities.user(1, Language.ES);
    private final User userEn = TestEntities.user(1, Language.EN);
    private final Store store1 = TestEntities.store(1, TestEntities.master(1));
    private final Store store2 = TestEntities.store(2, TestEntities.master(2));

    @BeforeEach
    void setUp() {
        profileStrategy = new ProfileBasedStrategy(tasteProfileRepository, publicCatalog);
        interactionStrategy = new InteractionBasedStrategy(interactionRepository, factoryProvider, publicCatalog);
        when(catalogRepository.findAllArtworkIds()).thenReturn(List.of(10L, 11L, 20L));
        hybridStrategy = new HybridStrategy(profileStrategy, interactionStrategy, interactionRepository);
        when(artworkRepository.findByDeletedAtIsNullAndStatusInOrderByArtworkIdAsc(anyCollection(), any()))
                .thenReturn(List.of(
                TestEntities.artwork(10, store1, ArtworkStatus.EXHIBITED, "rojo", "verde"),
                TestEntities.artwork(11, store1, ArtworkStatus.EXHIBITED, "dorado"),
                TestEntities.artwork(20, store2, ArtworkStatus.EXHIBITED, "azul", "rojo")));
    }

    private void givenPreferredColors(String... colors) {
        TasteProfile profile = new TasteProfile();
        for (String color : colors) {
            profile.getColors().add(TestEntities.color(color));
        }
        when(tasteProfileRepository.findByUserUserId(1L)).thenReturn(Optional.of(profile));
    }

    private void givenInteractions(StoreInteraction... interactions) {
        when(interactionRepository.findStoreInteractions(1L)).thenReturn(List.of(interactions));
        when(interactionRepository.countByUserUserId(1L)).thenReturn((long) interactions.length);
    }

    @Test
    void storeGetsTheScoreOfItsBestMatchingArtwork() {
        givenPreferredColors("rojo", "verde", "dorado", "negro");

        List<RecommendationResult> results = profileStrategy.recommend(userEs);

        // store 1: artwork 10 has rojo and verde (2 of 4 colors); store 2: artwork 20 has rojo (1 of 4)
        assertThat(results).extracting(RecommendationResult::storeId).containsExactly(1L, 2L);
        assertThat(results.get(0).score()).isEqualByComparingTo("50.00");
        assertThat(results.get(1).score()).isEqualByComparingTo("25.00");
        assertThat(results.get(0).reason()).isEqualTo("Coincide con: colores rojo, verde");
    }

    @Test
    void profileReasonFollowsUserLanguage() {
        givenPreferredColors("azul");

        List<RecommendationResult> results = profileStrategy.recommend(userEn);

        assertThat(results).singleElement().satisfies(r -> {
            assertThat(r.storeId()).isEqualTo(2);
            assertThat(r.score()).isEqualByComparingTo("100");
            assertThat(r.reason()).isEqualTo("Matches: colors azul");
        });
    }

    @Test
    void profileWithoutTasteProfileGivesNothing() {
        when(tasteProfileRepository.findByUserUserId(1L)).thenReturn(Optional.empty());

        assertThat(profileStrategy.recommend(userEs)).isEmpty();
    }

    @Test
    void interactionsUseFactoryWeightsAndDuration() {
        // store 1: ZOOM (3) with 300 s -> 3 x 2 = 6 points; store 2: VIEW (1) without duration bonus -> 1 point
        givenInteractions(interaction(1, InteractionAction.ZOOM, 300), interaction(2, InteractionAction.VIEW, 0));

        List<RecommendationResult> results = interactionStrategy.recommend(userEs);

        assertThat(results).extracting(RecommendationResult::storeId).containsExactly(1L, 2L);
        assertThat(results.get(0).score()).isEqualByComparingTo("100");
        assertThat(results.get(1).score()).isEqualByComparingTo("16.67");
    }

    @Test
    void fallbackStrategiesOnlySuggestThePublicCatalog() {
        givenPreferredColors("rojo");
        StoreInteraction visit = mock(StoreInteraction.class);
        when(visit.getStoreId()).thenReturn(2L);
        when(visit.getAction()).thenReturn(InteractionAction.STORE_VISIT);
        givenInteractions(visit);
        when(catalogRepository.findAllArtworkIds()).thenReturn(List.of(10L, 11L));

        assertThat(profileStrategy.recommendArtworks(userEs)).extracting(RecommendationResult::artworkId)
                .containsExactly(10L);
        assertThat(interactionStrategy.recommend(userEs)).isEmpty();
        assertThat(interactionStrategy.recommendArtworks(userEs)).isEmpty();
    }

    @Test
    void withoutPublicArtworksEveryFallbackStrategyIsEmpty() {
        givenPreferredColors("rojo");
        StoreInteraction visit = mock(StoreInteraction.class);
        when(visit.getStoreId()).thenReturn(1L);
        when(visit.getAction()).thenReturn(InteractionAction.STORE_VISIT);
        givenInteractions(visit);
        when(catalogRepository.findAllArtworkIds()).thenReturn(List.of());

        assertThat(profileStrategy.recommend(userEs)).isEmpty();
        assertThat(interactionStrategy.recommend(userEs)).isEmpty();
        assertThat(hybridStrategy.recommend(userEs)).isEmpty();
        assertThat(hybridStrategy.recommendArtworks(userEs)).isEmpty();
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
        givenInteractions(interaction(1, InteractionAction.ZOOM, null));

        List<RecommendationResult> results = hybridStrategy.recommend(userEs);

        assertThat(results).singleElement().satisfies(r -> {
            assertThat(r.storeId()).isEqualTo(2);
            assertThat(r.score()).isEqualByComparingTo("100");
        });
    }

    @Test
    void hybridMixesSixtyPercentInteractionsAndFortyPercentProfile() {
        givenPreferredColors("azul");
        givenInteractions(interaction(1, InteractionAction.ZOOM, null), interaction(1, InteractionAction.ZOOM, null),
                interaction(1, InteractionAction.ZOOM, null));

        List<RecommendationResult> results = hybridStrategy.recommend(userEs);

        // store 1: 0.6 x 100 + 0.4 x 0 = 60; store 2: 0.6 x 0 + 0.4 x 100 = 40
        assertThat(results).extracting(RecommendationResult::storeId).containsExactly(1L, 2L);
        assertThat(results).extracting(RecommendationResult::score)
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactly(new BigDecimal("60"), new BigDecimal("40"));
    }

    @Test
    void aiStrategyParsesValidLinesOnly() {
        String answer = """
                2|88|Great match
                99|70|Unknown store
                nonsense
                1|abc|bad score
                1|150|Clamped""";

        List<RecommendationResult> results = AiStrategy.parse(answer, Set.of(1L, 2L));

        assertThat(results).extracting(RecommendationResult::storeId).containsExactly(2L, 1L);
        assertThat(results.get(1).score()).isEqualByComparingTo("100");
    }

    @Test
    void aiStrategyRefusesToRunWhenAiIsDisabled() {
        AiStrategy aiStrategy = new AiStrategy(new DisabledAiClient(), publicCatalog, tasteProfileRepository,
                hybridStrategy);

        assertThatThrownBy(() -> aiStrategy.recommend(userEs)).isInstanceOf(FeatureDisabledException.class);
    }

    @Test
    void aiStrategyOnlyOffersStoresOfThePublicCatalog() {
        AiTextClient ai = mock(AiTextClient.class);
        when(ai.isEnabled()).thenReturn(true);
        when(ai.complete(anyString(), anyString())).thenReturn("1|90|Colores que te gustan\n2|80|Tienda sin publicar");
        when(catalogRepository.findAllArtworkIds()).thenReturn(List.of(10L, 11L));
        AiStrategy aiStrategy = new AiStrategy(ai, publicCatalog, tasteProfileRepository, hybridStrategy);

        List<RecommendationResult> results = aiStrategy.recommend(userEs);

        assertThat(results).extracting(RecommendationResult::storeId).containsExactly(1L);
        ArgumentCaptor<String> prompt = ArgumentCaptor.forClass(String.class);
        verify(ai).complete(anyString(), prompt.capture());
        assertThat(prompt.getValue()).contains("1 | Store 1").doesNotContain("2 | Store 2");
    }

    @Test
    void aiStrategyWithoutPublicArtworksIsEmptyAndDoesNotCallTheProvider() {
        AiTextClient ai = mock(AiTextClient.class);
        when(ai.isEnabled()).thenReturn(true);
        when(catalogRepository.findAllArtworkIds()).thenReturn(List.of());
        AiStrategy aiStrategy = new AiStrategy(ai, publicCatalog, tasteProfileRepository, hybridStrategy);

        assertThat(aiStrategy.recommend(userEs)).isEmpty();
        assertThat(aiStrategy.recommendArtworks(userEs)).isEmpty();
        verify(ai, never()).complete(anyString(), anyString());
    }

    @Test
    void resolverPicksByNameAndDefaultsToHybrid() {
        AiTextClient ai = new DisabledAiClient();
        AiStrategy aiStrategy = new AiStrategy(ai, publicCatalog, tasteProfileRepository, hybridStrategy);
        RecommendationStrategyResolver resolver = new RecommendationStrategyResolver(
                List.of(profileStrategy, interactionStrategy, hybridStrategy, aiStrategy), ai);

        assertThat(resolver.resolve(null)).isSameAs(hybridStrategy);
        assertThat(resolver.resolve("PROFILE")).isSameAs(profileStrategy);
        assertThat(resolver.resolve("interactions")).isSameAs(interactionStrategy);
        assertThatThrownBy(() -> resolver.resolve("ai")).isInstanceOf(FeatureDisabledException.class);
        assertThatThrownBy(() -> resolver.resolve("magic")).isInstanceOf(BusinessRuleException.class);
    }

    private static StoreInteraction interaction(long storeId, InteractionAction action, Integer duration) {
        StoreInteraction interaction = mock(StoreInteraction.class);
        when(interaction.getStoreId()).thenReturn(storeId);
        when(interaction.getAction()).thenReturn(action);
        when(interaction.getDurationSeconds()).thenReturn(duration);
        return interaction;
    }
}
