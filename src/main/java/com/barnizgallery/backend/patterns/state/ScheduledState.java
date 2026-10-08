package com.barnizgallery.backend.patterns.state;

import com.barnizgallery.backend.dto.request.BidRequest;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.model.enums.ArtworkStatus;
import com.barnizgallery.backend.model.enums.AuctionStatus;

/**
 * <b>State pattern – ConcreteState "programada".</b>
 * <p>
 * The auction exists but has not started. It can start (→ active) or be cancelled
 * (→ cancelled). Bids and finishing are not allowed.
 */
public class ScheduledState implements AuctionState {

    @Override
    public void start(AuctionContext context) {
        context.setArtworkStatus(ArtworkStatus.IN_AUCTION);
        context.transitionTo(AuctionStateFactory.from(AuctionStatus.ACTIVE));
    }

    @Override
    public void placeBid(AuctionContext context, BidRequest request) {
        throw new BusinessRuleException("The auction has not started yet");
    }

    @Override
    public void finish(AuctionContext context) {
        throw new BusinessRuleException("A scheduled auction cannot be finished; cancel it instead");
    }

    @Override
    public void cancel(AuctionContext context) {
        context.setArtworkStatus(ArtworkStatus.EXHIBITED);
        context.transitionTo(AuctionStateFactory.from(AuctionStatus.CANCELLED));
    }

    @Override
    public AuctionStatus status() {
        return AuctionStatus.SCHEDULED;
    }
}
