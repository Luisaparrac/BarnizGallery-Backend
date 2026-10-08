package com.barnizgallery.backend.patterns.state;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.dto.BidRequest;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Auction;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.AuctionStatus;
import com.barnizgallery.backend.exception.BusinessRuleException;

class AuctionStateTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 8, 12, 0);
    private static final BidRequest BID = new BidRequest(1, new BigDecimal("10"), "USD");

    private Artwork artwork;

    private AuctionContext context(AuctionStatus status, long bids) {
        ArtworkStatus artworkStatus = status == AuctionStatus.ACTIVE ? ArtworkStatus.IN_AUCTION
                : ArtworkStatus.EXHIBITED;
        artwork = TestEntities.artwork(1, TestEntities.room(1, TestEntities.master(1)), artworkStatus);
        Auction auction = TestEntities.auction(1, artwork, status, NOW.minusDays(1), NOW.plusDays(1), "100");
        return new AuctionContext(auction, () -> bids);
    }

    @Test
    void scheduledStartsAndPutsArtworkInAuction() {
        AuctionContext ctx = context(AuctionStatus.SCHEDULED, 0);

        ctx.start();

        assertThat(ctx.getStatus()).isEqualTo(AuctionStatus.ACTIVE);
        assertThat(ctx.getAuction().getStatus()).isEqualTo(AuctionStatus.ACTIVE);
        assertThat(artwork.getStatus()).isEqualTo(ArtworkStatus.IN_AUCTION);
    }

    @Test
    void scheduledRejectsBidsAndFinish() {
        AuctionContext ctx = context(AuctionStatus.SCHEDULED, 0);

        assertThatThrownBy(() -> ctx.placeBid(BID)).isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(ctx::finish).isInstanceOf(BusinessRuleException.class);
        assertThat(ctx.getStatus()).isEqualTo(AuctionStatus.SCHEDULED);
    }

    @Test
    void scheduledCanBeCancelled() {
        AuctionContext ctx = context(AuctionStatus.SCHEDULED, 0);

        ctx.cancel();

        assertThat(ctx.getStatus()).isEqualTo(AuctionStatus.CANCELLED);
        assertThat(artwork.getStatus()).isEqualTo(ArtworkStatus.EXHIBITED);
    }

    @Test
    void activeAcceptsBidsAndCannotStartAgain() {
        AuctionContext ctx = context(AuctionStatus.ACTIVE, 0);

        assertThatCode(() -> ctx.placeBid(BID)).doesNotThrowAnyException();
        assertThatThrownBy(ctx::start).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void activeFinishedWithBidsSellsTheArtwork() {
        AuctionContext ctx = context(AuctionStatus.ACTIVE, 3);

        ctx.finish();

        assertThat(ctx.getStatus()).isEqualTo(AuctionStatus.FINISHED);
        assertThat(artwork.getStatus()).isEqualTo(ArtworkStatus.SOLD);
    }

    @Test
    void activeFinishedWithoutBidsReturnsArtworkToExhibition() {
        AuctionContext ctx = context(AuctionStatus.ACTIVE, 0);

        ctx.finish();

        assertThat(artwork.getStatus()).isEqualTo(ArtworkStatus.EXHIBITED);
    }

    @Test
    void activeCancelledReturnsArtworkToExhibition() {
        AuctionContext ctx = context(AuctionStatus.ACTIVE, 2);

        ctx.cancel();

        assertThat(ctx.getStatus()).isEqualTo(AuctionStatus.CANCELLED);
        assertThat(artwork.getStatus()).isEqualTo(ArtworkStatus.EXHIBITED);
    }

    @ParameterizedTest
    @EnumSource(value = AuctionStatus.class, names = { "FINISHED", "CANCELLED" })
    void finalStatesRejectEverything(AuctionStatus status) {
        AuctionContext ctx = context(status, 0);

        assertThatThrownBy(ctx::start).isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> ctx.placeBid(BID)).isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(ctx::finish).isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(ctx::cancel).isInstanceOf(BusinessRuleException.class);
        assertThat(ctx.getStatus()).isEqualTo(status);
    }

    @Test
    void syncStartsAndFinishesAnAuctionThatExpiredWhileSleeping() {
        AuctionContext ctx = context(AuctionStatus.SCHEDULED, 0);

        assertThat(ctx.syncWithClock(NOW.plusDays(2)))
                .containsExactly(AuctionStatus.ACTIVE, AuctionStatus.FINISHED);
        assertThat(artwork.getStatus()).isEqualTo(ArtworkStatus.EXHIBITED);
    }

    @Test
    void syncDoesNothingWhenNotDue() {
        AuctionContext ctx = context(AuctionStatus.ACTIVE, 0);

        assertThat(ctx.syncWithClock(NOW)).isEmpty();
        assertThat(ctx.getStatus()).isEqualTo(AuctionStatus.ACTIVE);
    }
}
