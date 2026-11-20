package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.dto.AiBidAssessRequest;
import com.barnizgallery.backend.dto.AiBidAssessment;
import com.barnizgallery.backend.entity.Auction;
import com.barnizgallery.backend.entity.Bid;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.exception.AiServiceException;
import com.barnizgallery.backend.patterns.adapter.AiServiceGateway;
import com.barnizgallery.backend.patterns.facade.BidVerdict;
import com.barnizgallery.backend.repository.BidRepository;

/** The AI bid assessment only marks bids, and a failure of the service never blocks them. */
class BidRiskAssessorTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 11, 20, 19, 0);

    private final AiServiceGateway ai = mock(AiServiceGateway.class);
    private final BidRepository bids = mock(BidRepository.class);
    private final BidRiskAssessor assessor = new BidRiskAssessor(ai, bids,
            Clock.fixed(Instant.parse("2026-11-20T19:20:00Z"), ZoneOffset.UTC));
    private final Auction auction = new Auction();
    private final User bidder = TestEntities.user(81, Language.ES);
    private final User other = TestEntities.user(55, Language.ES);

    @BeforeEach
    void history() {
        auction.setAuctionId(7L);
        auction.setBasePrice(new BigDecimal("100"));
        auction.setCurrency("USD");
        auction.setEndDate(T0.plusHours(1));
        bidder.setCreatedAt(Instant.parse("2026-11-20T15:00:00Z"));
        when(bids.findByAuctionWithUser(7L)).thenReturn(List.of(
                bid(other, "130", 3), bid(bidder, "120", 2), bid(bidder, "110", 1)));
        when(ai.isAvailable()).thenReturn(true);
    }

    @Test
    void theRequestDescribesTheLeaderAndTheOwnRaises() {
        AiBidAssessRequest request = assessor.request(auction, new BigDecimal("1200"), bidder, BigDecimal.ONE);

        assertThat(request.auction().currentBid()).isEqualByComparingTo("130");
        assertThat(request.auction().leaderBidderId()).isEqualTo(55L);
        assertThat(request.auction().lastBidAt()).isEqualTo(Instant.parse("2026-11-20T19:03:00Z"));
        assertThat(request.bid().placedAt()).isEqualTo(Instant.parse("2026-11-20T19:20:00Z"));
        assertThat(request.bidder().bids()).extracting(b -> b.raisedOwn()).containsExactly(false, true);
    }

    @Test
    void aSuspiciousAnswerMarksTheBidWithTheReasons() {
        when(ai.assessBid(any())).thenReturn(new AiBidAssessment("SUSPICIOUS", new BigDecimal("0.99"),
                List.of("Salto de 10 veces la puja actual"), "model"));

        var assessment = assessor.assess(auction, new BigDecimal("1200"), bidder, BigDecimal.ONE);

        assertThat(assessment.verdict()).isEqualTo(BidVerdict.SUSPICIOUS);
        assertThat(assessment.reason()).isEqualTo("AI: Salto de 10 veces la puja actual");
    }

    @Test
    void aFailureOrADisabledServiceLetsTheBidThrough() {
        when(ai.assessBid(any())).thenThrow(new AiServiceException(503, "service_unreachable", "down"));
        assertThat(assessor.assess(auction, BigDecimal.TEN, bidder, BigDecimal.ONE).verdict()).isEqualTo(BidVerdict.OK);

        when(ai.isAvailable()).thenReturn(false);
        assertThat(assessor.assess(auction, BigDecimal.TEN, bidder, BigDecimal.ONE).verdict()).isEqualTo(BidVerdict.OK);
        verify(ai, never()).assessBid(null);
    }

    private static Bid bid(User user, String amount, int minute) {
        Bid bid = new Bid();
        bid.setUser(user);
        bid.setAmount(new BigDecimal(amount));
        bid.setBidDate(T0.plusMinutes(minute));
        return bid;
    }
}
