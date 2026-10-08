package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.dto.InteractionRequest;
import com.barnizgallery.backend.dto.InteractionResponse;
import com.barnizgallery.backend.entity.Interaction;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.InteractionAction;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.patterns.factorymethod.BidInteractionFactory;
import com.barnizgallery.backend.patterns.factorymethod.InteractionFactoryProvider;
import com.barnizgallery.backend.patterns.factorymethod.RotateInteractionFactory;
import com.barnizgallery.backend.patterns.factorymethod.TouchInteractionFactory;
import com.barnizgallery.backend.patterns.factorymethod.ViewInteractionFactory;
import com.barnizgallery.backend.repository.InteractionRepository;

@ExtendWith(MockitoExtension.class)
class InteractionServiceTest {

    @Mock
    private InteractionRepository interactionRepository;
    @Mock
    private VisitorService visitorService;
    @Mock
    private ArtworkService artworkService;
    private InteractionService interactionService;

    @BeforeEach
    void setUp() {
        InteractionFactoryProvider provider = new InteractionFactoryProvider(List.of(new ViewInteractionFactory(),
                new TouchInteractionFactory(), new RotateInteractionFactory(), new BidInteractionFactory()));
        interactionService = new InteractionService(interactionRepository, provider, visitorService, artworkService);
    }

    @Test
    void recordUsesTheFactoryOfTheAction() {
        when(visitorService.getVisitor(1)).thenReturn(TestEntities.visitor(1, Language.ES));
        when(artworkService.getArtwork(2)).thenReturn(
                TestEntities.artwork(2, TestEntities.room(1, TestEntities.master(1)), ArtworkStatus.EXHIBITED));
        when(interactionRepository.save(any(Interaction.class))).thenAnswer(inv -> inv.getArgument(0));

        InteractionResponse response = interactionService.record(
                new InteractionRequest(1, 2, InteractionAction.ROTATE, null));

        assertThat(response.action()).isEqualTo(InteractionAction.ROTATE);
        assertThat(response.artworkId()).isEqualTo(2);
    }

    @Test
    void bidCannotBeRecordedFromTheEndpoint() {
        assertThatThrownBy(() -> interactionService.record(new InteractionRequest(1, 2, InteractionAction.BID, null)))
                .isInstanceOf(BusinessRuleException.class);
        verify(interactionRepository, never()).save(any());
    }
}
