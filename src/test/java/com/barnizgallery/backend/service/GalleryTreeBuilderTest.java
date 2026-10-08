package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.dto.GalleryNodeResponse;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Room;
import com.barnizgallery.backend.entity.ThreeDModel;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.GenerationStatus;
import com.barnizgallery.backend.patterns.composite.GalleryComposite;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.PhotoRepository;
import com.barnizgallery.backend.repository.RoomRepository;
import com.barnizgallery.backend.repository.ThreeDModelRepository;

@ExtendWith(MockitoExtension.class)
class GalleryTreeBuilderTest {

    @Mock
    private RoomRepository roomRepository;
    @Mock
    private ArtworkRepository artworkRepository;
    @Mock
    private PhotoRepository photoRepository;
    @Mock
    private ThreeDModelRepository threeDModelRepository;
    @InjectMocks
    private GalleryTreeBuilder builder;

    @Test
    void buildsRoomsWithTheirArtworksThumbnailsAndModels() {
        Room room1 = TestEntities.room(1, TestEntities.master(1));
        Room room2 = TestEntities.room(2, TestEntities.master(2));
        Artwork a1 = TestEntities.artwork(10, room1, ArtworkStatus.EXHIBITED);
        Artwork a2 = TestEntities.artwork(11, room1, ArtworkStatus.IN_AUCTION);
        Artwork a3 = TestEntities.artwork(12, room2, ArtworkStatus.SOLD);
        ThreeDModel model = new ThreeDModel();
        model.setArtwork(a1);
        model.setGenerationStatus(GenerationStatus.COMPLETED);
        model.setGlbFileUrl("a1.glb");
        when(roomRepository.findAllWithMaster()).thenReturn(List.of(room1, room2));
        when(artworkRepository.findAllByOrderByArtworkIdAsc()).thenReturn(List.of(a1, a2, a3));
        when(photoRepository.findFirstPhotoOfEachArtwork()).thenReturn(List.of(TestEntities.photo(1, a2, "a2.jpg")));
        when(threeDModelRepository.findByGenerationStatus(GenerationStatus.COMPLETED)).thenReturn(List.of(model));

        GalleryComposite gallery = builder.build();
        GalleryNodeResponse root = gallery.toNode();

        assertThat(root.artworkCount()).isEqualTo(3);
        assertThat(root.availableForAuctionCount()).isEqualTo(1);
        assertThat(root.children()).extracting(GalleryNodeResponse::artworkCount).containsExactly(2, 1);
        GalleryNodeResponse first = root.children().get(0).children().get(0);
        GalleryNodeResponse second = root.children().get(0).children().get(1);
        assertThat(first.glbFileUrl()).isEqualTo("a1.glb");
        assertThat(first.thumbnailUrl()).isNull();
        assertThat(second.thumbnailUrl()).isEqualTo("a2.jpg");
    }

    @Test
    void emptyDatabaseGivesEmptyGallery() {
        when(roomRepository.findAllWithMaster()).thenReturn(List.of());
        when(artworkRepository.findAllByOrderByArtworkIdAsc()).thenReturn(List.of());
        when(photoRepository.findFirstPhotoOfEachArtwork()).thenReturn(List.of());
        when(threeDModelRepository.findByGenerationStatus(GenerationStatus.COMPLETED)).thenReturn(List.of());

        GalleryNodeResponse root = builder.build().toNode();

        assertThat(root.children()).isEmpty();
        assertThat(root.artworkCount()).isZero();
    }
}
