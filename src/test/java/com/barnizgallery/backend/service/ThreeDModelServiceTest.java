package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.dto.ThreeDModelResponse;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.ThreeDModel;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.GenerationStatus;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.exception.FeatureDisabledException;
import com.barnizgallery.backend.patterns.adapter.GenerationTicket;
import com.barnizgallery.backend.patterns.adapter.ThreeDModelGenerator;
import com.barnizgallery.backend.repository.PhotoRepository;
import com.barnizgallery.backend.repository.ThreeDModelRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ThreeDModelServiceTest {

    @Mock
    private ThreeDModelRepository threeDModelRepository;
    @Mock
    private PhotoRepository photoRepository;
    @Mock
    private ArtworkService artworkService;
    @Mock
    private ThreeDModelGenerator generator;
    @InjectMocks
    private ThreeDModelService service;

    private Artwork artwork;

    @BeforeEach
    void setUp() {
        artwork = TestEntities.artwork(4, TestEntities.room(1, TestEntities.master(1)), ArtworkStatus.EXHIBITED);
        when(artworkService.getArtwork(4)).thenReturn(artwork);
        when(generator.isEnabled()).thenReturn(true);
        when(threeDModelRepository.save(any(ThreeDModel.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private ThreeDModel model(GenerationStatus status, String taskId) {
        ThreeDModel model = new ThreeDModel();
        model.setArtwork(artwork);
        model.setGenerationStatus(status);
        model.setHyper3dTaskId(taskId);
        when(threeDModelRepository.findByArtworkArtworkId(4)).thenReturn(Optional.of(model));
        return model;
    }

    @Test
    void generateIs503WhenHyper3dIsDisabled() {
        when(generator.isEnabled()).thenReturn(false);

        assertThatThrownBy(() -> service.generate(4)).isInstanceOf(FeatureDisabledException.class);
    }

    @Test
    void generateNeedsAtLeastOnePhoto() {
        when(threeDModelRepository.findByArtworkArtworkId(4)).thenReturn(Optional.empty());
        when(photoRepository.findByArtworkArtworkIdOrderByPhotoIdAsc(4)).thenReturn(List.of());

        assertThatThrownBy(() -> service.generate(4)).hasMessageContaining("at least one photo");
        verify(generator, never()).submit(any());
    }

    @Test
    void generateIs409WhenAlreadyCompleted() {
        model(GenerationStatus.COMPLETED, "t");

        assertThatThrownBy(() -> service.generate(4))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already completed");
    }

    @Test
    void generateCreatesProcessingModel() {
        when(threeDModelRepository.findByArtworkArtworkId(4)).thenReturn(Optional.empty());
        when(photoRepository.findByArtworkArtworkIdOrderByPhotoIdAsc(4))
                .thenReturn(List.of(TestEntities.photo(1, artwork, "https://img/1.jpg")));
        when(generator.submit(List.of("https://img/1.jpg"))).thenReturn(new GenerationTicket("task", "key"));

        ThreeDModelResponse response = service.generate(4);

        assertThat(response.generationStatus()).isEqualTo(GenerationStatus.PROCESSING);
        assertThat(response.hyper3dTaskId()).isEqualTo("task|key");
        assertThat(response.artworkId()).isEqualTo(4);
    }

    @Test
    void refreshStoresGlbUrlWhenDone() {
        ThreeDModel model = model(GenerationStatus.PROCESSING, "task|key");
        GenerationTicket ticket = new GenerationTicket("task", "key");
        when(generator.checkStatus(ticket)).thenReturn(GenerationStatus.COMPLETED);
        when(generator.fetchGlbUrl(ticket)).thenReturn(Optional.of("https://cdn/model.glb"));

        service.refresh(4);

        assertThat(model.getGenerationStatus()).isEqualTo(GenerationStatus.COMPLETED);
        assertThat(model.getGlbFileUrl()).isEqualTo("https://cdn/model.glb");
    }

    @Test
    void refreshMarksFailedTasks() {
        ThreeDModel model = model(GenerationStatus.PROCESSING, "task|key");
        when(generator.checkStatus(any())).thenReturn(GenerationStatus.FAILED);

        service.refresh(4);

        assertThat(model.getGenerationStatus()).isEqualTo(GenerationStatus.FAILED);
    }
}
