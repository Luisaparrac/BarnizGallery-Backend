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
import com.barnizgallery.backend.entity.Store;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.patterns.composite.GalleryComposite;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.PhotoRepository;
import com.barnizgallery.backend.repository.StoreRepository;

@ExtendWith(MockitoExtension.class)
class GalleryTreeBuilderTest {

    @Mock
    private StoreRepository storeRepository;
    @Mock
    private ArtworkRepository artworkRepository;
    @Mock
    private PhotoRepository photoRepository;
    @InjectMocks
    private GalleryTreeBuilder builder;

    @Test
    void buildsStoresWithTheirArtworksAndThumbnails() {
        Store store1 = TestEntities.store(1, TestEntities.master(1));
        Store store2 = TestEntities.store(2, TestEntities.master(2));
        Artwork a1 = TestEntities.artwork(10, store1, ArtworkStatus.EXHIBITED);
        Artwork a2 = TestEntities.artwork(11, store1, ArtworkStatus.IN_AUCTION);
        Artwork a3 = TestEntities.artwork(12, store2, ArtworkStatus.SOLD);
        when(storeRepository.findAllWithMaster()).thenReturn(List.of(store1, store2));
        when(artworkRepository.findAllByOrderByArtworkIdAsc()).thenReturn(List.of(a1, a2, a3));
        when(photoRepository.findFirstPhotoOfEachArtwork()).thenReturn(List.of(TestEntities.photo(1, a2, "a2.jpg")));

        GalleryComposite gallery = builder.build();
        GalleryNodeResponse root = gallery.toNode();

        assertThat(root.artworkCount()).isEqualTo(3);
        assertThat(root.availableForAuctionCount()).isEqualTo(1);
        assertThat(root.children()).extracting(GalleryNodeResponse::artworkCount).containsExactly(2, 1);
        GalleryNodeResponse first = root.children().get(0).children().get(0);
        GalleryNodeResponse second = root.children().get(0).children().get(1);
        assertThat(first.thumbnailUrl()).isNull();
        assertThat(second.thumbnailUrl()).isEqualTo("a2.jpg");
    }

    @Test
    void emptyDatabaseGivesEmptyGallery() {
        when(storeRepository.findAllWithMaster()).thenReturn(List.of());
        when(artworkRepository.findAllByOrderByArtworkIdAsc()).thenReturn(List.of());
        when(photoRepository.findFirstPhotoOfEachArtwork()).thenReturn(List.of());

        GalleryNodeResponse root = builder.build().toNode();

        assertThat(root.children()).isEmpty();
        assertThat(root.artworkCount()).isZero();
    }
}
