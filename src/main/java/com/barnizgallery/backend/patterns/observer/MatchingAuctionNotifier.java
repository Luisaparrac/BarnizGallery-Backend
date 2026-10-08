package com.barnizgallery.backend.patterns.observer;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

import com.barnizgallery.backend.dto.response.VisitorNotification;
import com.barnizgallery.backend.model.entity.TasteProfile;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.TasteProfileRepository;

/**
 * <b>Observer pattern – ConcreteObserver.</b>
 * <p>
 * When an auction starts, notifies the visitors whose preferred colors match the
 * {@code color_tags} of the auctioned artwork.
 */
@Component
public class MatchingAuctionNotifier implements AuctionObserver {

    private final ArtworkRepository artworkRepository;
    private final TasteProfileRepository tasteProfileRepository;
    private final SimpMessageSendingOperations messaging;

    public MatchingAuctionNotifier(ArtworkRepository artworkRepository,
            TasteProfileRepository tasteProfileRepository, SimpMessageSendingOperations messaging) {
        this.artworkRepository = artworkRepository;
        this.tasteProfileRepository = tasteProfileRepository;
        this.messaging = messaging;
    }

    @Override
    public void onEvent(AuctionEvent event) {
        if (event.type() != AuctionEventType.AUCTION_STARTED) {
            return;
        }
        artworkRepository.findById(event.artworkId()).ifPresent(artwork -> {
            Set<String> artworkColors = normalize(artwork.getColorTags());
            if (artworkColors.isEmpty()) {
                return;
            }
            for (TasteProfile profile : tasteProfileRepository.findAll()) {
                boolean matches = normalize(profile.getPreferredColors()).stream().anyMatch(artworkColors::contains);
                if (matches) {
                    VisitorNotification notification = new VisitorNotification(
                            VisitorNotification.MATCHING_AUCTION, event.auctionId(), event.artworkId(),
                            "An auction matching your tastes has started: " + artwork.getTitleEn());
                    messaging.convertAndSend("/topic/visitors/" + profile.getVisitor().getVisitorId()
                            + "/notifications", notification);
                }
            }
        });
    }

    private static Set<String> normalize(List<String> values) {
        if (values == null) {
            return Set.of();
        }
        return values.stream().filter(Objects::nonNull).map(v -> v.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }
}
