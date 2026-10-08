package com.barnizgallery.backend.patterns.builder;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;

import com.barnizgallery.backend.model.entity.Artwork;
import com.barnizgallery.backend.model.entity.Auction;

/**
 * <b>Builder pattern – Director.</b>
 * <p>
 * Knows common "recipes" for auctions and drives the {@link AuctionBuilder}
 * through the right steps, so callers do not repeat them.
 */
public class AuctionDirector {

    static final int STANDARD_DAYS = 7;

    private final Clock clock;

    public AuctionDirector(Clock clock) {
        this.clock = clock;
    }

    /** An auction that starts now and lasts seven days. */
    public Auction standardSevenDayAuction(AuctionBuilder builder, Artwork artwork, BigDecimal basePrice) {
        LocalDateTime now = LocalDateTime.now(clock);
        return builder.forArtwork(artwork)
                .basePrice(basePrice)
                .startsAt(now)
                .endsAt(now.plusDays(STANDARD_DAYS))
                .build();
    }
}
