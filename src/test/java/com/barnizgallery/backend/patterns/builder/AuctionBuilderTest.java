package com.barnizgallery.backend.patterns.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Auction;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.AuctionStatus;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.patterns.facade.AiFacade;
import com.barnizgallery.backend.repository.AuctionRepository;

class AuctionBuilderTest {

    private static final ZoneId ZONE = ZoneId.of("UTC");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 8, 12, 0);
    private final Clock clock = Clock.fixed(NOW.atZone(ZONE).toInstant(), ZONE);
    private final AuctionRepository auctionRepository = mock(AuctionRepository.class);
    private final Artwork artwork = TestEntities.artwork(5, TestEntities.room(1, TestEntities.master(1)),
            ArtworkStatus.EXHIBITED);

    private AuctionBuilder builder() {
        return new AuctionBuilder(auctionRepository, clock);
    }

    @Test
    void futureStartGivesScheduledAuction() {
        Auction auction = builder().forArtwork(artwork).basePrice(new BigDecimal("500"))
                .startsAt(NOW.plusDays(1)).endsAt(NOW.plusDays(3)).build();

        assertThat(auction.getStatus()).isEqualTo(AuctionStatus.SCHEDULED);
        assertThat(auction.getArtwork()).isSameAs(artwork);
        assertThat(auction.getBasePrice()).isEqualByComparingTo("500");
    }

    @Test
    void startInThePastGivesActiveAuction() {
        Auction auction = builder().forArtwork(artwork).basePrice(BigDecimal.ZERO)
                .startsAt(NOW.minusMinutes(1)).endsAt(NOW.plusDays(1)).build();

        assertThat(auction.getStatus()).isEqualTo(AuctionStatus.ACTIVE);
    }

    @Test
    void artworkMustBeExhibited() {
        artwork.setStatus(ArtworkStatus.SOLD);

        assertThatThrownBy(() -> builder().forArtwork(artwork).basePrice(BigDecimal.TEN)
                .startsAt(NOW).endsAt(NOW.plusDays(1)).build())
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("must be exhibited");
    }

    @Test
    void artworkCannotHaveAnotherOpenAuction() {
        when(auctionRepository.existsByArtworkArtworkIdAndStatusIn(eq(5), anyCollection())).thenReturn(true);

        assertThatThrownBy(() -> builder().forArtwork(artwork).basePrice(BigDecimal.TEN)
                .startsAt(NOW).endsAt(NOW.plusDays(1)).build())
                .hasMessageContaining("already has a scheduled or active auction");
    }

    @Test
    void endMustBeAfterStart() {
        assertThatThrownBy(() -> builder().forArtwork(artwork).basePrice(BigDecimal.TEN)
                .startsAt(NOW.plusDays(2)).endsAt(NOW.plusDays(1)).build())
                .hasMessageContaining("endDate must be after startDate");
    }

    @Test
    void basePriceCannotBeNegative() {
        assertThatThrownBy(() -> builder().forArtwork(artwork).basePrice(new BigDecimal("-1"))
                .startsAt(NOW).endsAt(NOW.plusDays(1)).build())
                .hasMessageContaining("basePrice must be >= 0");
    }

    @Test
    void missingPriceWithAiDisabledFails() {
        AiFacade ai = mock(AiFacade.class);
        when(ai.isAiEnabled()).thenReturn(false);

        assertThatThrownBy(() -> builder().forArtwork(artwork).useSuggestedBasePrice(ai)
                .startsAt(NOW).endsAt(NOW.plusDays(1)).build())
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("basePrice is required while AI is disabled");
    }

    @Test
    void missingPriceUsesAiSuggestionWhenEnabled() {
        AiFacade ai = mock(AiFacade.class);
        when(ai.isAiEnabled()).thenReturn(true);
        when(ai.suggestBasePrice(artwork)).thenReturn(Optional.of(new BigDecimal("750.00")));

        Auction auction = builder().forArtwork(artwork).useSuggestedBasePrice(ai)
                .startsAt(NOW.plusHours(1)).endsAt(NOW.plusDays(1)).build();

        assertThat(auction.getBasePrice()).isEqualByComparingTo("750");
    }
}
