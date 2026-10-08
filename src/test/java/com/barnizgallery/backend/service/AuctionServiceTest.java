package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.dto.request.CreateAuctionRequest;
import com.barnizgallery.backend.model.entity.Artwork;
import com.barnizgallery.backend.model.entity.Auction;
import com.barnizgallery.backend.model.enums.ArtworkStatus;
import com.barnizgallery.backend.model.enums.AuctionStatus;
import com.barnizgallery.backend.patterns.facade.AiFacade;
import com.barnizgallery.backend.repository.AuctionRepository;
import com.barnizgallery.backend.repository.BidRepository;

class AuctionServiceTest {

    private static final ZoneId ZONE = ZoneId.of("UTC");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 8, 12, 0);

    private final AuctionRepository auctionRepository = mock(AuctionRepository.class);
    private final BidRepository bidRepository = mock(BidRepository.class);
    private final ArtworkService artworkService = mock(ArtworkService.class);
    private final AuctionResponseAssembler assembler = mock(AuctionResponseAssembler.class);
    private AuctionService auctionService;
    private Artwork artwork;

    @BeforeEach
    void setUp() {
        auctionService = new AuctionService(auctionRepository, bidRepository, artworkService, mock(AiFacade.class),
                assembler, Clock.fixed(NOW.atZone(ZONE).toInstant(), ZONE));
        artwork = TestEntities.artwork(5, TestEntities.room(1, TestEntities.master(1)), ArtworkStatus.EXHIBITED);
        when(artworkService.getArtwork(5)).thenReturn(artwork);
        when(auctionRepository.existsByArtworkArtworkIdAndStatusIn(eq(5), anyCollection())).thenReturn(false);
        when(auctionRepository.save(any(Auction.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void creatingAnAuctionThatStartsNowPutsArtworkInAuction() {
        auctionService.create(new CreateAuctionRequest(5, new BigDecimal("100"), NOW, NOW.plusDays(2)));

        assertThat(artwork.getStatus()).isEqualTo(ArtworkStatus.IN_AUCTION);
    }

    @Test
    void creatingAFutureAuctionKeepsArtworkExhibited() {
        auctionService.create(new CreateAuctionRequest(5, new BigDecimal("100"), NOW.plusDays(1), NOW.plusDays(2)));

        assertThat(artwork.getStatus()).isEqualTo(ArtworkStatus.EXHIBITED);
    }

    @Test
    void syncDueAuctionsCountsChangedAuctions() {
        Auction due = TestEntities.auction(1, artwork, AuctionStatus.SCHEDULED, NOW.minusHours(1),
                NOW.plusDays(1), "100");
        when(auctionRepository.findDueForTransition(NOW)).thenReturn(List.of(due));

        assertThat(auctionService.syncDueAuctions()).isEqualTo(1);
        assertThat(due.getStatus()).isEqualTo(AuctionStatus.ACTIVE);
    }

    @Test
    void readingAnExpiredActiveAuctionFinishesIt() {
        artwork.setStatus(ArtworkStatus.IN_AUCTION);
        Auction expired = TestEntities.auction(1, artwork, AuctionStatus.ACTIVE, NOW.minusDays(3),
                NOW.minusDays(1), "100");
        when(auctionRepository.findByIdWithArtwork(1)).thenReturn(Optional.of(expired));
        when(bidRepository.countByAuctionAuctionId(1)).thenReturn(2L);

        auctionService.getSyncedAuction(1);

        assertThat(expired.getStatus()).isEqualTo(AuctionStatus.FINISHED);
        assertThat(artwork.getStatus()).isEqualTo(ArtworkStatus.SOLD);
    }
}
