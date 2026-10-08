package com.barnizgallery.backend.patterns.observer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.dto.response.BidResponse;
import com.barnizgallery.backend.dto.response.VisitorNotification;
import com.barnizgallery.backend.model.entity.Artwork;
import com.barnizgallery.backend.model.entity.TasteProfile;
import com.barnizgallery.backend.model.enums.ArtworkStatus;
import com.barnizgallery.backend.model.enums.Language;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.TasteProfileRepository;

class AuctionObserversTest {

    private final SimpMessageSendingOperations messaging = mock(SimpMessageSendingOperations.class);

    private static AuctionEvent bidEvent(int bidderId, Integer previousTopBidderId) {
        BidResponse bid = new BidResponse(10, 1, bidderId, "Visitor", new BigDecimal("500"), "USD",
                LocalDateTime.now());
        return AuctionEvent.bidPlaced(1, 7, bid, previousTopBidderId);
    }

    @Test
    void publisherNotifiesEveryObserver() {
        List<AuctionEvent> received = new ArrayList<>();
        AuctionObserver first = received::add;
        AuctionObserver second = received::add;
        AuctionEventPublisher publisher = new AuctionEventPublisher(List.of(first, second));

        publisher.publish(AuctionEvent.of(AuctionEventType.AUCTION_STARTED, 1, 7));

        assertThat(received).hasSize(2);
    }

    @Test
    void aFailingObserverDoesNotStopTheOthers() {
        List<AuctionEvent> received = new ArrayList<>();
        AuctionObserver failing = event -> {
            throw new IllegalStateException("boom");
        };
        AuctionEventPublisher publisher = new AuctionEventPublisher(List.of(failing, received::add));

        publisher.publish(AuctionEvent.of(AuctionEventType.AUCTION_CANCELLED, 1, 7));

        assertThat(received).hasSize(1);
    }

    @Test
    void broadcasterSendsEventToAuctionTopic() {
        AuctionEvent event = bidEvent(3, null);

        new LiveAuctionBroadcaster(messaging).onEvent(event);

        verify(messaging).convertAndSend("/topic/auctions/1", event);
    }

    @Test
    void outbidNotifierWarnsThePreviousTopBidder() {
        new OutbidNotifier(messaging).onEvent(bidEvent(3, 8));

        ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);
        verify(messaging).convertAndSend(eq("/topic/visitors/8/notifications"), payload.capture());
        assertThat(((VisitorNotification) payload.getValue()).type()).isEqualTo(VisitorNotification.OUTBID);
    }

    @Test
    void outbidNotifierStaysQuietWithoutAnotherBidder() {
        OutbidNotifier notifier = new OutbidNotifier(messaging);

        notifier.onEvent(bidEvent(3, null));
        notifier.onEvent(bidEvent(3, 3));
        notifier.onEvent(AuctionEvent.of(AuctionEventType.AUCTION_STARTED, 1, 7));

        verify(messaging, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void matchingNotifierWarnsVisitorsWithMatchingColors() {
        ArtworkRepository artworkRepository = mock(ArtworkRepository.class);
        TasteProfileRepository tasteProfileRepository = mock(TasteProfileRepository.class);
        Artwork artwork = TestEntities.artwork(7, TestEntities.room(1, TestEntities.master(1)),
                ArtworkStatus.IN_AUCTION, "Rojo", "dorado");
        when(artworkRepository.findById(7)).thenReturn(Optional.of(artwork));
        when(tasteProfileRepository.findAll()).thenReturn(List.of(profile(1, "rojo"), profile(2, "azul"),
                profile(3, "DORADO", "verde")));

        new MatchingAuctionNotifier(artworkRepository, tasteProfileRepository, messaging)
                .onEvent(AuctionEvent.of(AuctionEventType.AUCTION_STARTED, 1, 7));

        verify(messaging).convertAndSend(eq("/topic/visitors/1/notifications"), any(Object.class));
        verify(messaging).convertAndSend(eq("/topic/visitors/3/notifications"), any(Object.class));
        verify(messaging, times(2)).convertAndSend(anyString(), any(Object.class));
    }

    private static TasteProfile profile(int visitorId, String... colors) {
        TasteProfile profile = new TasteProfile();
        profile.setVisitor(TestEntities.visitor(visitorId, Language.ES));
        profile.setPreferredColors(List.of(colors));
        return profile;
    }
}
