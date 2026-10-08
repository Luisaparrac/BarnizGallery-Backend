package com.barnizgallery.backend.patterns.state;

import com.barnizgallery.backend.dto.BidRequest;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.AuctionStatus;
import com.barnizgallery.backend.exception.BusinessRuleException;

/**
 * <b>State pattern – ConcreteState "activa".</b>
 * <p>
 * The only state that accepts bids. It can finish (→ finished: the artwork is sold
 * if there are bids, otherwise it goes back to exhibited) or be cancelled.
 */
public class ActiveState implements AuctionState {

    @Override
    public void start(AuctionContext context) {
        throw new BusinessRuleException("The auction is already active");
    }

    @Override
    public void placeBid(AuctionContext context, BidRequest request) {
        // Allowed: the amount, currency and frequency rules are checked by the bid service.
    }

    @Override
    public void finish(AuctionContext context) {
        context.setArtworkStatus(context.hasBids() ? ArtworkStatus.SOLD : ArtworkStatus.EXHIBITED);
        context.transitionTo(AuctionStateFactory.from(AuctionStatus.FINISHED));
    }

    @Override
    public void cancel(AuctionContext context) {
        context.setArtworkStatus(ArtworkStatus.EXHIBITED);
        context.transitionTo(AuctionStateFactory.from(AuctionStatus.CANCELLED));
    }

    @Override
    public AuctionStatus status() {
        return AuctionStatus.ACTIVE;
    }
}
