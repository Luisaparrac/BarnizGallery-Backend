package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.inOrder;
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
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.patterns.facade.AiFacade;
import com.barnizgallery.backend.patterns.strategy.RecommendationResult;
import com.barnizgallery.backend.repository.RecommendationRepository;
import com.barnizgallery.backend.repository.RoomRepository;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private AiFacade aiFacade;
    @Mock
    private RecommendationRepository recommendationRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private UserService userService;
    @InjectMocks
    private RecommendationService recommendationService;

    @Test
    void computeReplacesPreviousRecommendations() {
        User user = TestEntities.user(1, Language.ES);
        when(userService.getUser(1L)).thenReturn(user);
        when(aiFacade.recommendRooms(user, "profile")).thenReturn(List.of(
                RecommendationResult.of(2L, 80, "reason 2"), RecommendationResult.of(1L, 40, "reason 1")));
        when(roomRepository.findAllById(List.of(2L, 1L))).thenReturn(List.of(
                TestEntities.room(1, TestEntities.master(1)), TestEntities.room(2, TestEntities.master(2))));
        when(recommendationRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<RecommendationResponse> saved = recommendationService.compute(1L, "profile");

        assertThat(saved).extracting(RecommendationResponse::roomId).containsExactly(2L, 1L);
        assertThat(saved.get(0).roomNameEn()).isEqualTo("Room 2");
        InOrder order = inOrder(recommendationRepository);
        order.verify(recommendationRepository).deleteByUserId(1L);
        order.verify(recommendationRepository).saveAll(anyList());
    }
}
