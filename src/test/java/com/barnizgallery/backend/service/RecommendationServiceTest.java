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
import com.barnizgallery.backend.entity.Visitor;
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
    private VisitorService visitorService;
    @InjectMocks
    private RecommendationService recommendationService;

    @Test
    void computeReplacesPreviousRecommendations() {
        Visitor visitor = TestEntities.visitor(1, Language.ES);
        when(visitorService.getVisitor(1)).thenReturn(visitor);
        when(aiFacade.recommendRooms(visitor, "profile")).thenReturn(List.of(
                RecommendationResult.of(2, 80, "reason 2"), RecommendationResult.of(1, 40, "reason 1")));
        when(roomRepository.findAllById(List.of(2, 1))).thenReturn(List.of(
                TestEntities.room(1, TestEntities.master(1)), TestEntities.room(2, TestEntities.master(2))));
        when(recommendationRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<RecommendationResponse> saved = recommendationService.compute(1, "profile");

        assertThat(saved).extracting(RecommendationResponse::roomId).containsExactly(2, 1);
        assertThat(saved.get(0).roomNameEn()).isEqualTo("Room 2");
        InOrder order = inOrder(recommendationRepository);
        order.verify(recommendationRepository).deleteByVisitorId(1);
        order.verify(recommendationRepository).saveAll(anyList());
    }
}
