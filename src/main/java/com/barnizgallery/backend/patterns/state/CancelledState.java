package com.barnizgallery.backend.patterns.state;

import com.barnizgallery.backend.dto.BidRequest;
import com.barnizgallery.backend.enums.AuctionStatus;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.BusinessRuleException;

/**
 * <b>State pattern – ConcreteState "cancelada".</b>
 * <p>
 * Final state: every operation is rejected.
 */
public class CancelledState implements AuctionState {

    @Override
    public void start(AuctionContext context) {
        throw new BusinessRuleException(ErrorCode.AUCTION_CANCELLED);
    }

    @Override
    public void placeBid(AuctionContext context, BidRequest request) {
        throw new BusinessRuleException(ErrorCode.AUCTION_CANCELLED);
    }

    @Override
    public void finish(AuctionContext context) {
        throw new BusinessRuleException(ErrorCode.AUCTION_CANCELLED);
    }

    @Override
    public void cancel(AuctionContext context) {
        throw new BusinessRuleException(ErrorCode.AUCTION_CANCELLED);
    }

    @Override
    public AuctionStatus status() {
        return AuctionStatus.CANCELLED;
    }
}
