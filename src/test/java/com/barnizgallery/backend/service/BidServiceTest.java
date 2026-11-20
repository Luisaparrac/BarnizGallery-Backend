package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.TestSettings;
import com.barnizgallery.backend.dto.AnomalyAlert;
import com.barnizgallery.backend.dto.BidRequest;
import com.barnizgallery.backend.dto.BidResponse;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Auction;
import com.barnizgallery.backend.entity.Bid;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.AuctionStatus;
import com.barnizgallery.backend.enums.BidReviewStatus;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.patterns.facade.AiFacade;
import com.barnizgallery.backend.patterns.facade.BidAssessment;
import com.barnizgallery.backend.patterns.observer.AuctionEvent;
import com.barnizgallery.backend.patterns.observer.AuctionEventPublisher;
import com.barnizgallery.backend.patterns.observer.AuctionEventType;
import com.barnizgallery.backend.patterns.state.AuctionContext;
import com.barnizgallery.backend.repository.BidRepository;

class BidServiceTest {

    private static final ZoneId ZONE = ZoneId.of("UTC");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 8, 12, 0);

    private final BidRepository bidRepository = mock(BidRepository.class);
    private final AuctionService auctionService = mock(AuctionService.class);
    private final UserService userService = mock(UserService.class);
    private final AiFacade aiFacade = mock(AiFacade.class);
    private final AuctionEventPublisher publisher = mock(AuctionEventPublisher.class);
    private final SimpMessageSendingOperations messaging = mock(SimpMessageSendingOperations.class);
    private final AppSettingsService properties = TestSettings.auctionRules();
    private final BidRiskAssessor riskAssessor = mock(BidRiskAssessor.class);

    private BidService bidService;
    private Auction auction;
    private final User user = TestEntities.user(3, Language.ES);

    @BeforeEach
    void setUp() {
        bidService = new BidService(bidRepository, auctionService, userService, aiFacade, publisher, messaging,
                properties, riskAssessor, Clock.fixed(NOW.atZone(ZONE).toInstant(), ZONE));
        when(riskAssessor.assess(any(), any(), any(), any())).thenReturn(BidAssessment.ok());
        givenAuction(AuctionStatus.ACTIVE);
        when(userService.getUser(3L)).thenReturn(user);
        when(aiFacade.assessBid(any(), any(), eq(3L))).thenReturn(BidAssessment.ok());
        when(bidRepository.save(any(Bid.class))).thenAnswer(inv -> {
            Bid bid = inv.getArgument(0);
            bid.setBidId(99L);
            return bid;
        });
    }

    private void givenAuction(AuctionStatus status) {
        Artwork artwork = TestEntities.artwork(7, TestEntities.store(1, TestEntities.master(1)),
                ArtworkStatus.IN_AUCTION);
        auction = TestEntities.auction(1, artwork, status, NOW.minusDays(1), NOW.plusDays(1), "100");
        when(auctionService.getSyncedAuctionForUpdate(1L)).thenReturn(auction);
        when(auctionService.contextFor(auction)).thenReturn(new AuctionContext(auction, () -> 0));
    }

    private void givenTopBid(String amount, int userId) {
        Bid top = new Bid();
        top.setAmount(new BigDecimal(amount));
        top.setUser(TestEntities.user(userId, Language.EN));
        when(bidRepository.findFirstByAuctionAuctionIdOrderByAmountDescBidDateAsc(1L)).thenReturn(Optional.of(top));
    }

    private static BidRequest bid(String amount, String currency) {
        return new BidRequest(3L, new BigDecimal(amount), currency);
    }

    @Test
    void validBidIsSavedAndPublishedWithPreviousTopBidder() {
        givenTopBid("150", 8);

        BidResponse saved = bidService.placeBid(1L, bid("151", "usd"));

        assertThat(saved.bidId()).isEqualTo(99);
        assertThat(saved.currency()).isEqualTo("USD");
        ArgumentCaptor<AuctionEvent> event = ArgumentCaptor.forClass(AuctionEvent.class);
        verify(publisher).publish(event.capture());
        assertThat(event.getValue().type()).isEqualTo(AuctionEventType.BID_PLACED);
        assertThat(event.getValue().previousTopBidderId()).isEqualTo(8);
        assertThat(event.getValue().artworkId()).isEqualTo(7);
    }

    @Test
    void firstBidMustBeatBasePriceByTheMinimumIncrement() {
        when(bidRepository.findFirstByAuctionAuctionIdOrderByAmountDescBidDateAsc(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bidService.placeBid(1L, bid("100.50", "USD")))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT))
                .hasMessage("bid_too_low [101, USD]");
        assertThat(bidService.placeBid(1L, bid("101", "USD")).amount()).isEqualByComparingTo("101");
    }

    @Test
    void bidMustBeatTheHighestBid() {
        givenTopBid("300", 8);

        assertThatThrownBy(() -> bidService.placeBid(1L, bid("300.99", "USD")))
                .hasMessage("bid_too_low [301, USD]");
        verify(bidRepository, never()).save(any());
    }

    @Test
    void otherCurrencyIsRejected() {
        assertThatThrownBy(() -> bidService.placeBid(1L, bid("500", "COP")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("bid_currency_mismatch [USD]");
    }

    @Test
    void historicalAuctionWithoutCurrencyRejectsBidsWithConflict() {
        auction.setCurrency(null);

        assertThatThrownBy(() -> bidService.placeBid(1L, bid("500", "COP")))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT))
                .hasMessageContaining("auction_without_currency");
        verify(bidRepository, never()).save(any());
    }

    @Test
    void bidFloodingIs429() {
        when(aiFacade.assessBid(any(), any(), eq(3L))).thenReturn(BidAssessment.rejected("Too many bids"));

        assertThatThrownBy(() -> bidService.placeBid(1L, bid("500", "USD")))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS));
        verify(bidRepository, never()).save(any());
    }

    @Test
    void suspiciousBidIsAcceptedAndReportedToAdmins() {
        when(aiFacade.assessBid(any(), any(), eq(3L))).thenReturn(BidAssessment.suspicious("huge"));

        bidService.placeBid(1L, bid("5000", "USD"));

        verify(bidRepository).save(any(Bid.class));
        verify(messaging).convertAndSend(eq(BidService.ANOMALIES_TOPIC), any(AnomalyAlert.class));
    }

    @Test
    void aBidTheAiServiceFindsSuspiciousIsSavedWithTheMark() {
        when(riskAssessor.assess(any(), any(), any(), any())).thenReturn(BidAssessment.suspicious("AI: salto"));

        bidService.placeBid(1L, bid("500", "USD"));

        ArgumentCaptor<Bid> saved = ArgumentCaptor.forClass(Bid.class);
        verify(bidRepository).save(saved.capture());
        assertThat(saved.getValue().getReviewStatus()).isEqualTo(BidReviewStatus.SUSPICIOUS);
        assertThat(saved.getValue().getReviewReason()).isEqualTo("AI: salto");
    }

    @Test
    void scheduledAuctionRejectsBidsThroughTheState() {
        givenAuction(AuctionStatus.SCHEDULED);

        assertThatThrownBy(() -> bidService.placeBid(1L, bid("500", "USD")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("auction_not_started");
        verify(messaging, never()).convertAndSend(anyString(), any(Object.class));
        verify(publisher, never()).publish(any());
    }
}
