package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.dto.PhotoRequest;
import com.barnizgallery.backend.dto.PhotoResponse;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Photo;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.exception.FeatureDisabledException;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.patterns.adapter.StorageService;
import com.barnizgallery.backend.repository.PhotoRepository;

@ExtendWith(MockitoExtension.class)
class PhotoServiceTest {

    @Mock
    private PhotoRepository photoRepository;
    @Mock
    private ArtworkService artworkService;
    @Mock
    private StorageService storageService;
    @InjectMocks
    private PhotoService photoService;

    @Test
    void addByUrlLinksPhotoToArtwork() {
        Artwork artwork = TestEntities.artwork(3, TestEntities.room(1, TestEntities.master(1)),
                ArtworkStatus.EXHIBITED);
        when(artworkService.getArtwork(3)).thenReturn(artwork);
        when(photoRepository.save(any(Photo.class))).thenAnswer(inv -> inv.getArgument(0));

        PhotoResponse photo = photoService.addByUrl(3, new PhotoRequest("https://img/front.jpg", "front"));

        assertThat(photo.artworkId()).isEqualTo(3);
        assertThat(photo.fileUrl()).isEqualTo("https://img/front.jpg");
        assertThat(photo.angle()).isEqualTo("front");
    }

    @Test
    void uploadIsDisabledWithoutStorage() {
        when(storageService.isEnabled()).thenReturn(false);

        assertThatThrownBy(() -> photoService.upload(3, null, null))
                .isInstanceOf(FeatureDisabledException.class);
    }

    @Test
    void deleteMissingPhotoIs404() {
        when(photoRepository.findById(5)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> photoService.delete(5)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteExistingPhoto() {
        Photo photo = new Photo();
        when(photoRepository.findById(5)).thenReturn(Optional.of(photo));

        photoService.delete(5);

        verify(photoRepository).delete(photo);
    }
}
