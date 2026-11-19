package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.dto.ArtworkDetailResponse;
import com.barnizgallery.backend.dto.ArtworkRequest;
import com.barnizgallery.backend.dto.ArtworkSummaryResponse;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Store;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.AuctionRepository;
import com.barnizgallery.backend.repository.PhotoRepository;

@ExtendWith(MockitoExtension.class)
class ArtworkServiceTest {

    @Mock
    private ArtworkRepository artworkRepository;
    @Mock
    private PhotoRepository photoRepository;
    @Mock
    private AuctionRepository auctionRepository;
    @Mock
    private AuctionResponseAssembler auctionAssembler;
    @Mock
    private StoreService storeService;
    @InjectMocks
    private ArtworkService artworkService;

    private final Store store = TestEntities.store(1, TestEntities.master(1));

    @Test
    void findAllUsesTheRightFilter() {
        when(artworkRepository.findByStoreStoreIdAndStatusOrderByArtworkIdAsc(1L, ArtworkStatus.SOLD))
                .thenReturn(List.of(TestEntities.artwork(7, store, ArtworkStatus.SOLD)));

        List<ArtworkSummaryResponse> result = artworkService.findAll(1L, ArtworkStatus.SOLD);

        assertThat(result).singleElement().satisfies(a -> {
            assertThat(a.artworkId()).isEqualTo(7);
            assertThat(a.status()).isEqualTo(ArtworkStatus.SOLD);
        });
    }

    @Test
    void findAllWithoutFiltersReturnsEverything() {
        when(artworkRepository.findAllByOrderByArtworkIdAsc()).thenReturn(List.of());

        assertThat(artworkService.findAll(null, null)).isEmpty();
        verify(artworkRepository).findAllByOrderByArtworkIdAsc();
    }

    @Test
    void detailIncludesPhotosAndNoAuctionWhenAbsent() {
        Artwork artwork = TestEntities.artwork(3, store, ArtworkStatus.EXHIBITED);
        when(artworkRepository.findById(3L)).thenReturn(Optional.of(artwork));
        when(photoRepository.findByArtworkArtworkIdOrderByPhotoIdAsc(3L))
                .thenReturn(List.of(TestEntities.photo(1, artwork, "https://img/1.jpg")));
        when(auctionRepository.findFirstByArtworkArtworkIdAndStatusInOrderByStartDateDesc(eq(3L), anyCollection()))
                .thenReturn(Optional.empty());

        ArtworkDetailResponse detail = artworkService.findById(3L);

        assertThat(detail.photos()).hasSize(1);
        assertThat(detail.currentAuction()).isNull();
    }

    @Test
    void createStartsAsExhibited() {
        when(storeService.getStore(1L)).thenReturn(store);
        when(artworkRepository.save(any(Artwork.class))).thenAnswer(inv -> inv.getArgument(0));

        ArtworkSummaryResponse created = artworkService.create(
                new ArtworkRequest(1L, "Obra", "Artwork", null, null, null, null, List.of("red"), List.of()));

        assertThat(created.status()).isEqualTo(ArtworkStatus.EXHIBITED);
        assertThat(created.colorTags()).containsExactly("red");
    }

    @Test
    void updateDoesNotChangeStatus() {
        Artwork artwork = TestEntities.artwork(3, store, ArtworkStatus.IN_AUCTION);
        when(artworkRepository.findById(3L)).thenReturn(Optional.of(artwork));

        ArtworkSummaryResponse updated = artworkService.update(3L,
                new ArtworkRequest(1L, "Nuevo", "New", null, null, null, null, null, null));

        assertThat(updated.titleEn()).isEqualTo("New");
        assertThat(updated.status()).isEqualTo(ArtworkStatus.IN_AUCTION);
    }

    @Test
    void missingArtworkIs404() {
        when(artworkRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> artworkService.getArtwork(99L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
