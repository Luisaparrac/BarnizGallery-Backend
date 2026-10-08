package com.barnizgallery.backend.patterns.state;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;

import com.barnizgallery.backend.dto.request.BidRequest;
import com.barnizgallery.backend.model.entity.Artwork;
import com.barnizgallery.backend.model.entity.Auction;
import com.barnizgallery.backend.model.enums.ArtworkStatus;
import com.barnizgallery.backend.model.enums.AuctionStatus;

/**
 * <b>State pattern – Context.</b>
 * <p>
 * Wraps one {@link Auction} entity and keeps its current {@link AuctionState}.
 * Clients call {@link #start()}, {@link #finish()}, {@link #cancel()} or
 * {@link #placeBid(BidRequest)}; the context forwards the call to the current state,
 * which decides what happens and may change the state with {@link #transitionTo}.
 */
public class AuctionContext {

    private final Auction auction;
    private final LongSupplier bidCounter;
    private AuctionState state;

    /**
     * @param auction    the auction entity (its status is updated on every transition)
     * @param bidCounter tells how many bids the auction has (used when it finishes)
     */
    public AuctionContext(Auction auction, LongSupplier bidCounter) {
        this.auction = auction;
        this.bidCounter = bidCounter;
        this.state = AuctionStateFactory.from(auction.getStatus());
    }

    public void start() {
        state.start(this);
    }

    public void placeBid(BidRequest request) {
        state.placeBid(this, request);
    }

    public void finish() {
        state.finish(this);
    }

    public void cancel() {
        state.cancel(this);
    }

    /**
     * Applies the transitions that are due at {@code now}: a scheduled auction whose start
     * date has passed is started, and an active auction whose end date has passed is finished.
     * Needed because Render's free plan sleeps and the scheduler does not run while it sleeps.
     *
     * @return the statuses reached, in order (empty if nothing changed)
     */
    public List<AuctionStatus> syncWithClock(LocalDateTime now) {
        List<AuctionStatus> reached = new ArrayList<>();
        if (getStatus() == AuctionStatus.SCHEDULED && !auction.getStartDate().isAfter(now)) {
            start();
            reached.add(getStatus());
        }
        if (getStatus() == AuctionStatus.ACTIVE && !auction.getEndDate().isAfter(now)) {
            finish();
            reached.add(getStatus());
        }
        return reached;
    }

    /** Called by the states to move to another state. Keeps the entity status in sync. */
    void transitionTo(AuctionState next) {
        this.state = next;
        auction.setStatus(next.status());
    }

    /** Called by the states to update the status of the auctioned artwork. */
    void setArtworkStatus(ArtworkStatus status) {
        Artwork artwork = auction.getArtwork();
        artwork.setStatus(status);
    }

    boolean hasBids() {
        return bidCounter.getAsLong() > 0;
    }

    public AuctionStatus getStatus() {
        return state.status();
    }

    public Auction getAuction() {
        return auction;
    }
}
