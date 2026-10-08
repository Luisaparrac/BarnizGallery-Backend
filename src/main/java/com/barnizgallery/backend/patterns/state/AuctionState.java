package com.barnizgallery.backend.patterns.state;

import com.barnizgallery.backend.dto.request.BidRequest;
import com.barnizgallery.backend.model.enums.AuctionStatus;

/**
 * <b>State pattern – State.</b>
 * <p>
 * What can be done with an auction depends on its status. Instead of writing
 * {@code if (status == ...)} everywhere, each status is a class that implements
 * these operations: valid ones change the state, invalid ones throw
 * {@link com.barnizgallery.backend.exception.BusinessRuleException}.
 */
public interface AuctionState {

    void start(AuctionContext context);

    /** Checks that a bid can be placed now. Only the active state allows it. */
    void placeBid(AuctionContext context, BidRequest request);

    void finish(AuctionContext context);

    void cancel(AuctionContext context);

    /** The database status this state represents. */
    AuctionStatus status();
}
