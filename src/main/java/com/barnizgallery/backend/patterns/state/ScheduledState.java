package com.barnizgallery.backend.patterns.state;

import com.barnizgallery.backend.dto.BidRequest;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.AuctionStatus;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.BusinessRuleException;

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
        throw new BusinessRuleException(ErrorCode.AUCTION_NOT_STARTED);
    }

    @Override
    public void finish(AuctionContext context) {
        throw new BusinessRuleException(ErrorCode.SCHEDULED_AUCTION_CANNOT_FINISH);
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
