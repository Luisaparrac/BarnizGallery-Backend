package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.dto.RecommendationResponse;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.patterns.facade.AiFacade;
import com.barnizgallery.backend.patterns.strategy.RecommendationResult;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.RecommendationRepository;
import com.barnizgallery.backend.repository.StoreRepository;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private AiFacade aiFacade;
    @Mock
    private RecommendationRepository recommendationRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private UserService userService;
    @Mock
    private ArtworkRepository artworkRepository;
    @InjectMocks
    private RecommendationService recommendationService;

    @Test
    void computeReplacesPreviousRecommendations() {
        User user = TestEntities.user(1, Language.ES);
        when(userService.getUser(1L)).thenReturn(user);
        when(aiFacade.strategyName("profile")).thenReturn("profile");
        when(aiFacade.recommendStores(user, "profile")).thenReturn(List.of(
                RecommendationResult.of(2L, 80, "reason 2"), RecommendationResult.of(1L, 40, "reason 1")));
        when(storeRepository.findAllById(List.of(2L, 1L))).thenReturn(List.of(
                TestEntities.store(1, TestEntities.master(1)), TestEntities.store(2, TestEntities.master(2))));
        when(recommendationRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<RecommendationResponse> saved = recommendationService.compute(1L, "profile", RecommendationService.STORES);

        assertThat(saved).extracting(RecommendationResponse::storeId).containsExactly(2L, 1L);
        assertThat(saved.get(0).storeNameEn()).isEqualTo("Store 2");
        assertThat(saved.get(0).strategy()).isEqualTo("profile");
        InOrder order = inOrder(recommendationRepository);
        order.verify(recommendationRepository).deleteStoresByUserId(1L);
        order.verify(recommendationRepository).saveAll(anyList());
    }

    @Test
    void artworkRecommendationsReplaceOnlyArtworkRows() {
        User user = TestEntities.user(1, Language.ES);
        when(userService.getUser(1L)).thenReturn(user);
        when(aiFacade.recommendArtworks(user, "profile")).thenReturn(List.of(
                RecommendationResult.ofArtwork(10L, 90, "reason")));
        when(artworkRepository.findAllById(List.of(10L))).thenReturn(List.of(TestEntities.artwork(10,
                TestEntities.store(1, TestEntities.master(1)), ArtworkStatus.EXHIBITED)));
        when(recommendationRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<RecommendationResponse> saved = recommendationService.compute(1L, "profile", RecommendationService.ARTWORKS);

        assertThat(saved).singleElement().satisfies(r -> {
            assertThat(r.artworkId()).isEqualTo(10L);
            assertThat(r.storeId()).isNull();
        });
        verify(recommendationRepository).deleteArtworksByUserId(1L);
        verify(recommendationRepository, never()).deleteStoresByUserId(1L);
    }

    @Test
    void unknownTargetIs400() {
        when(userService.getUser(1L)).thenReturn(TestEntities.user(1, Language.ES));

        assertThatThrownBy(() -> recommendationService.compute(1L, "profile", "planets"))
                .hasMessage("unknown_recommendation_target [planets]");
    }
}
