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
import com.barnizgallery.backend.dto.request.ArtworkRequest;
import com.barnizgallery.backend.dto.response.ArtworkDetailResponse;
import com.barnizgallery.backend.dto.response.ArtworkSummaryResponse;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.model.entity.Artwork;
import com.barnizgallery.backend.model.entity.Room;
import com.barnizgallery.backend.model.enums.ArtworkStatus;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.AuctionRepository;
import com.barnizgallery.backend.repository.PhotoRepository;
import com.barnizgallery.backend.repository.ThreeDModelRepository;

@ExtendWith(MockitoExtension.class)
class ArtworkServiceTest {

    @Mock
    private ArtworkRepository artworkRepository;
    @Mock
    private PhotoRepository photoRepository;
    @Mock
    private ThreeDModelRepository threeDModelRepository;
    @Mock
    private AuctionRepository auctionRepository;
    @Mock
    private AuctionResponseAssembler auctionAssembler;
    @Mock
    private RoomService roomService;
    @InjectMocks
    private ArtworkService artworkService;

    private final Room room = TestEntities.room(1, TestEntities.master(1));

    @Test
    void findAllUsesTheRightFilter() {
        when(artworkRepository.findByRoomRoomIdAndStatusOrderByArtworkIdAsc(1, ArtworkStatus.SOLD))
                .thenReturn(List.of(TestEntities.artwork(7, room, ArtworkStatus.SOLD)));

        List<ArtworkSummaryResponse> result = artworkService.findAll(1, ArtworkStatus.SOLD);

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
    void detailIncludesPhotosAndNoModelOrAuctionWhenAbsent() {
        Artwork artwork = TestEntities.artwork(3, room, ArtworkStatus.EXHIBITED);
        when(artworkRepository.findById(3)).thenReturn(Optional.of(artwork));
        when(photoRepository.findByArtworkArtworkIdOrderByPhotoIdAsc(3))
                .thenReturn(List.of(TestEntities.photo(1, artwork, "https://img/1.jpg")));
        when(threeDModelRepository.findByArtworkArtworkId(3)).thenReturn(Optional.empty());
        when(auctionRepository.findFirstByArtworkArtworkIdAndStatusInOrderByStartDateDesc(eq(3), anyCollection()))
                .thenReturn(Optional.empty());

        ArtworkDetailResponse detail = artworkService.findById(3);

        assertThat(detail.photos()).hasSize(1);
        assertThat(detail.model()).isNull();
        assertThat(detail.currentAuction()).isNull();
    }

    @Test
    void createStartsAsExhibited() {
        when(roomService.getRoom(1)).thenReturn(room);
        when(artworkRepository.save(any(Artwork.class))).thenAnswer(inv -> inv.getArgument(0));

        ArtworkSummaryResponse created = artworkService.create(
                new ArtworkRequest(1, "Obra", "Artwork", null, null, null, null, List.of("red"), List.of()));

        assertThat(created.status()).isEqualTo(ArtworkStatus.EXHIBITED);
        assertThat(created.colorTags()).containsExactly("red");
    }

    @Test
    void updateDoesNotChangeStatus() {
        Artwork artwork = TestEntities.artwork(3, room, ArtworkStatus.IN_AUCTION);
        when(artworkRepository.findById(3)).thenReturn(Optional.of(artwork));

        ArtworkSummaryResponse updated = artworkService.update(3,
                new ArtworkRequest(1, "Nuevo", "New", null, null, null, null, null, null));

        assertThat(updated.titleEn()).isEqualTo("New");
        assertThat(updated.status()).isEqualTo(ArtworkStatus.IN_AUCTION);
    }

    @Test
    void missingArtworkIs404() {
        when(artworkRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> artworkService.getArtwork(99)).isInstanceOf(ResourceNotFoundException.class);
    }
}
