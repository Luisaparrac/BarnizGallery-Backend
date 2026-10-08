package com.barnizgallery.backend.patterns.state;

import com.barnizgallery.backend.dto.request.BidRequest;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.model.enums.AuctionStatus;

/**
 * <b>State pattern – ConcreteState "cancelada".</b>
 * <p>
 * Final state: every operation is rejected.
 */
public class CancelledState implements AuctionState {

    private static final String MESSAGE = "The auction was cancelled";

    @Override
    public void start(AuctionContext context) {
        throw new BusinessRuleException(MESSAGE);
    }

    @Override
    public void placeBid(AuctionContext context, BidRequest request) {
        throw new BusinessRuleException(MESSAGE);
    }

    @Override
    public void finish(AuctionContext context) {
        throw new BusinessRuleException(MESSAGE);
    }

    @Override
    public void cancel(AuctionContext context) {
        throw new BusinessRuleException(MESSAGE);
    }

    @Override
    public AuctionStatus status() {
        return AuctionStatus.CANCELLED;
    }
}
