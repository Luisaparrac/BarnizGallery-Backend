package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.dto.PhotoRequest;
import com.barnizgallery.backend.dto.PhotoResponse;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Photo;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.exception.FeatureDisabledException;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.patterns.adapter.StorageService;
import com.barnizgallery.backend.patterns.adapter.StoredImage;
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
        Artwork artwork = TestEntities.artwork(3, TestEntities.store(1, TestEntities.master(1)),
                ArtworkStatus.EXHIBITED);
        when(artworkService.getArtwork(3L)).thenReturn(artwork);
        when(photoRepository.save(any(Photo.class))).thenAnswer(inv -> inv.getArgument(0));

        PhotoResponse photo = photoService.addByUrl(3L, new PhotoRequest("https://img/front.jpg", "front"));

        assertThat(photo.artworkId()).isEqualTo(3);
        assertThat(photo.fileUrl()).isEqualTo("https://img/front.jpg");
        assertThat(photo.angle()).isEqualTo("front");
    }

    @Test
    void uploadIsDisabledWithoutStorage() {
        when(storageService.isEnabled()).thenReturn(false);

        assertThatThrownBy(() -> photoService.upload(3L, null, null))
                .isInstanceOf(FeatureDisabledException.class);
    }

    @Test
    void deleteMissingPhotoIs404() {
        when(photoRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> photoService.delete(5L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteExistingPhoto() {
        Photo photo = new Photo();
        when(photoRepository.findById(5L)).thenReturn(Optional.of(photo));

        photoService.delete(5L);

        verify(photoRepository).delete(photo);
        verify(storageService, never()).delete(any());
    }

    @Test
    void uploadSavesKeyUrlAndSize() {
        Artwork artwork = TestEntities.artwork(3, TestEntities.store(1, TestEntities.master(1)),
                ArtworkStatus.EXHIBITED);
        MultipartFile file = new MockMultipartFile("file", "front.jpg", "image/jpeg", new byte[] { 1 });
        when(storageService.isEnabled()).thenReturn(true);
        when(artworkService.getArtwork(3L)).thenReturn(artwork);
        when(storageService.upload(file, "artworks/3"))
                .thenReturn(new StoredImage("museo/artworks/3/abc", "https://res.test/abc.jpg", 800, 600));
        when(photoRepository.save(any(Photo.class))).thenAnswer(inv -> inv.getArgument(0));

        photoService.upload(3L, file, "front");

        ArgumentCaptor<Photo> saved = ArgumentCaptor.forClass(Photo.class);
        verify(photoRepository).save(saved.capture());
        assertThat(saved.getValue().getStorageKey()).isEqualTo("museo/artworks/3/abc");
        assertThat(saved.getValue().getFileUrl()).isEqualTo("https://res.test/abc.jpg");
        assertThat(saved.getValue().getWidthPx()).isEqualTo(800);
        assertThat(saved.getValue().getHeightPx()).isEqualTo(600);
    }

    @Test
    void deletingAnUploadedPhotoDeletesItsFile() {
        Photo photo = new Photo();
        photo.setStorageKey("museo/artworks/3/abc");
        when(photoRepository.findById(5L)).thenReturn(Optional.of(photo));
        when(storageService.isEnabled()).thenReturn(true);

        photoService.delete(5L);

        verify(storageService).delete("museo/artworks/3/abc");
    }
}
