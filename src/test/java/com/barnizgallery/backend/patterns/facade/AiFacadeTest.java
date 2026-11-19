package com.barnizgallery.backend.patterns.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.config.AuctionProperties;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Auction;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.AuctionStatus;
import com.barnizgallery.backend.patterns.adapter.AiTextClient;
import com.barnizgallery.backend.patterns.adapter.DisabledAiClient;
import com.barnizgallery.backend.patterns.strategy.RecommendationStrategyResolver;
import com.barnizgallery.backend.repository.BidRepository;

class AiFacadeTest {

    private final BidRepository bidRepository = mock(BidRepository.class);
    private final RecommendationStrategyResolver resolver = mock(RecommendationStrategyResolver.class);
    private final AuctionProperties properties = new AuctionProperties("USD", BigDecimal.ONE, 5, BigDecimal.TEN,
            false);

    private final Artwork artwork = TestEntities.artwork(3, TestEntities.store(1, TestEntities.master(1)),
            ArtworkStatus.IN_AUCTION);
    private final Auction auction = TestEntities.auction(7, artwork, AuctionStatus.ACTIVE,
            LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1), "100");

    private AiFacade facade(AiTextClient client) {
        return new AiFacade(resolver, client, bidRepository, properties);
    }

    @Test
    void suggestBasePriceIsEmptyWhenAiIsDisabled() {
        assertThat(facade(new DisabledAiClient()).suggestBasePrice(artwork)).isEmpty();
    }

    @Test
    void suggestBasePriceParsesTheAiAnswer() {
        AiTextClient client = mock(AiTextClient.class);
        when(client.isEnabled()).thenReturn(true);
        when(client.complete(anyString(), anyString())).thenReturn("About 1,250.5 USD");

        assertThat(facade(client).suggestBasePrice(artwork)).hasValueSatisfying(
                price -> assertThat(price).isEqualByComparingTo("1250.50"));
    }

    @Test
    void suggestBasePriceIgnoresAnswersWithoutANumber() {
        assertThat(AiFacade.parsePrice("I don't know")).isEmpty();
        assertThat(AiFacade.parsePrice("0")).isEmpty();
    }

    @Test
    void bidFloodingIsRejected() {
        when(bidRepository.countByUserUserIdAndBidDateAfter(eq(9L), any())).thenReturn(5L);

        BidAssessment assessment = facade(new DisabledAiClient()).assessBid(auction, new BigDecimal("150"), 9L);

        assertThat(assessment.verdict()).isEqualTo(BidVerdict.REJECTED);
    }

    @Test
    void hugeBidOverHighestIsSuspiciousButAccepted() {
        when(bidRepository.countByUserUserIdAndBidDateAfter(eq(9L), any())).thenReturn(0L);
        when(bidRepository.findHighestAmount(7L)).thenReturn(new BigDecimal("200"));

        BidAssessment assessment = facade(new DisabledAiClient()).assessBid(auction, new BigDecimal("2001"), 9L);

        assertThat(assessment.verdict()).isEqualTo(BidVerdict.SUSPICIOUS);
    }

    @Test
    void hugeBidOverBasePriceWhenNoBidsIsSuspicious() {
        when(bidRepository.countByUserUserIdAndBidDateAfter(eq(9L), any())).thenReturn(0L);
        when(bidRepository.findHighestAmount(7L)).thenReturn(null);

        assertThat(facade(new DisabledAiClient()).assessBid(auction, new BigDecimal("1001"), 9L).verdict())
                .isEqualTo(BidVerdict.SUSPICIOUS);
        assertThat(facade(new DisabledAiClient()).assessBid(auction, new BigDecimal("1000"), 9L).verdict())
                .isEqualTo(BidVerdict.OK);
    }

    @Test
    void aiIsNotCalledWhenDisabled() {
        AiTextClient client = mock(AiTextClient.class);
        when(client.isEnabled()).thenReturn(false);
        when(bidRepository.countByUserUserIdAndBidDateAfter(eq(9L), any())).thenReturn(0L);

        facade(client).assessBid(auction, new BigDecimal("150"), 9L);

        verify(client, never()).complete(anyString(), anyString());
    }

    @Test
    void aiCanMarkABidAsSuspicious() {
        AiTextClient client = mock(AiTextClient.class);
        when(client.isEnabled()).thenReturn(true);
        when(client.complete(anyString(), anyString())).thenReturn("SUSPICIOUS: unusual pattern");
        when(bidRepository.countByUserUserIdAndBidDateAfter(eq(9L), any())).thenReturn(0L);

        assertThat(facade(client).assessBid(auction, new BigDecimal("150"), 9L).verdict())
                .isEqualTo(BidVerdict.SUSPICIOUS);
    }
}
