package com.barnizgallery.backend.patterns.state;

import java.util.EnumMap;
import java.util.Map;

import com.barnizgallery.backend.model.enums.AuctionStatus;

/**
 * Returns the {@link AuctionState} object for a database status.
 * States have no fields, so one shared instance per status is enough.
 */
public final class AuctionStateFactory {

    private static final Map<AuctionStatus, AuctionState> STATES = new EnumMap<>(AuctionStatus.class);

    static {
        STATES.put(AuctionStatus.SCHEDULED, new ScheduledState());
        STATES.put(AuctionStatus.ACTIVE, new ActiveState());
        STATES.put(AuctionStatus.FINISHED, new FinishedState());
        STATES.put(AuctionStatus.CANCELLED, new CancelledState());
    }

    private AuctionStateFactory() {
    }

    public static AuctionState from(AuctionStatus status) {
        return STATES.get(status);
    }
}
