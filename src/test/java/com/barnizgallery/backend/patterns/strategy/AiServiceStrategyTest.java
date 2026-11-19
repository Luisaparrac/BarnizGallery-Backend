package com.barnizgallery.backend.patterns.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.dto.AiRecommendationItem;
import com.barnizgallery.backend.dto.AiRecommendationReasons;
import com.barnizgallery.backend.dto.AiRecommendationRequest;
import com.barnizgallery.backend.dto.AiRecommendationResponse;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Interaction;
import com.barnizgallery.backend.entity.Store;
import com.barnizgallery.backend.entity.TasteProfile;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.InteractionAction;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.exception.AiServiceException;
import com.barnizgallery.backend.patterns.adapter.AiServiceGateway;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.InteractionRepository;
import com.barnizgallery.backend.repository.TasteProfileRepository;

class AiServiceStrategyTest {

    private final AiServiceGateway aiService = mock(AiServiceGateway.class);
    private final HybridStrategy fallback = mock(HybridStrategy.class);
    private final ArtworkRepository artworkRepository = mock(ArtworkRepository.class);
    private final TasteProfileRepository tasteProfileRepository = mock(TasteProfileRepository.class);
    private final InteractionRepository interactionRepository = mock(InteractionRepository.class);
    private final AiServiceStrategy strategy = new AiServiceStrategy(aiService, fallback, artworkRepository,
            tasteProfileRepository, interactionRepository);

    private final User user = TestEntities.user(1, Language.ES);
    private final List<RecommendationResult> hybridResults = List.of(RecommendationResult.of(9L, 50, "hybrid"));
    private Artwork first;
    private Artwork second;
    private Artwork third;

    @BeforeEach
    void setUp() {
        Store storeOne = TestEntities.store(1, TestEntities.master(1));
        Store storeTwo = TestEntities.store(2, TestEntities.master(2));
        first = TestEntities.artwork(10, storeOne, ArtworkStatus.EXHIBITED, "rojo");
        second = TestEntities.artwork(11, storeOne, ArtworkStatus.EXHIBITED, "verde");
        third = TestEntities.artwork(20, storeTwo, ArtworkStatus.IN_AUCTION, "azul");
        when(aiService.isAvailable()).thenReturn(true);
        when(artworkRepository.findByDeletedAtIsNullAndStatusInOrderByArtworkIdAsc(anyCollection(), any()))
                .thenReturn(List.of(first, second, third));
        when(tasteProfileRepository.findByUserUserId(1L)).thenReturn(Optional.empty());
        when(fallback.recommend(user)).thenReturn(hybridResults);
    }

    @Test
    void addsUpArtworkScoresByStoreAndRescalesToOneHundred() {
        when(aiService.recommend(any())).thenReturn(new AiRecommendationResponse("content", List.of(
                item(10, 0.9, reasons(List.of("rojo"), true)),
                item(11, 0.6, reasons(List.of("rojo", "verde"), false)),
                item(20, 0.5, reasons(List.of(), false)))));

        List<RecommendationResult> results = strategy.recommend(user);

        assertThat(results).extracting(RecommendationResult::storeId).containsExactly(1L, 2L);
        assertThat(results.get(0).score()).isEqualByComparingTo("100.00");
        assertThat(results.get(1).score()).isEqualByComparingTo("33.33");
        assertThat(results.get(0).reason()).isEqualTo("Colores: rojo, verde. Formas y estilos que te gustan.");
        assertThat(results.get(1).reason()).isEqualTo("Obras populares en la galería.");
    }

    @Test
    void sendsOnlyInteractionsWithCandidatesAndLeavesBidsOut() {
        Artwork unavailable = TestEntities.artwork(99, first.getStore(), ArtworkStatus.SOLD);
        when(interactionRepository.findHistory(1L)).thenReturn(List.of(
                interaction(first, InteractionAction.VIEW, 40),
                interaction(third, InteractionAction.BID, null),
                interaction(unavailable, InteractionAction.ZOOM, null)));
        TasteProfile profile = new TasteProfile();
        profile.getColors().add(TestEntities.color("rojo"));
        when(tasteProfileRepository.findByUserUserId(1L)).thenReturn(Optional.of(profile));
        when(aiService.recommend(any())).thenReturn(new AiRecommendationResponse("hybrid", List.of()));

        strategy.recommend(user);

        ArgumentCaptor<AiRecommendationRequest> request = ArgumentCaptor.forClass(AiRecommendationRequest.class);
        verify(aiService).recommend(request.capture());
        assertThat(request.getValue().interactions()).singleElement().satisfies(i -> {
            assertThat(i.artworkId()).isEqualTo(10L);
            assertThat(i.action()).isEqualTo("VIEW");
            assertThat(i.durationSeconds()).isEqualTo(40);
        });
        assertThat(request.getValue().profile().colors()).containsExactly("rojo");
        assertThat(request.getValue().profile().budget()).isNull();
        assertThat(request.getValue().candidates()).hasSize(3);
        assertThat(request.getValue().limit()).isEqualTo(AiServiceStrategy.MAX_RESULTS);
    }

    @Test
    void fallsBackToHybridWhenTheServiceFails() {
        when(aiService.recommend(any())).thenThrow(new AiServiceException(504, "provider_timeout", "Timeout"));

        assertThat(strategy.recommend(user)).isEqualTo(hybridResults);
    }

    @Test
    void fallsBackToHybridWhenTheServiceIsDisabled() {
        when(aiService.isAvailable()).thenReturn(false);

        assertThat(strategy.recommend(user)).isEqualTo(hybridResults);
        verify(aiService, never()).recommend(any());
    }

    @Test
    void fallsBackToHybridWithoutAvailableArtworks() {
        when(artworkRepository.findByDeletedAtIsNullAndStatusInOrderByArtworkIdAsc(anyCollection(), any()))
                .thenReturn(List.of());

        assertThat(strategy.recommend(user)).isEqualTo(hybridResults);
        verify(aiService, never()).recommend(any());
    }

    private static AiRecommendationItem item(long artworkId, double score, AiRecommendationReasons reasons) {
        return new AiRecommendationItem(artworkId, null, score, reasons);
    }

    private static AiRecommendationReasons reasons(List<String> colors, boolean shape) {
        return new AiRecommendationReasons(colors, shape, false, List.of(), null);
    }

    private Interaction interaction(Artwork artwork, InteractionAction action, Integer duration) {
        Interaction interaction = new Interaction();
        interaction.setUser(user);
        interaction.setArtwork(artwork);
        interaction.setAction(action);
        interaction.setDurationSeconds(duration);
        return interaction;
    }
}
