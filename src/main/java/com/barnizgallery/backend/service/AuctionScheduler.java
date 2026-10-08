package com.barnizgallery.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Every 60 seconds, starts the scheduled auctions whose start date has arrived and
 * finishes the active ones whose end date has passed. Disabled with
 * {@code app.auction.scheduler-enabled=false}.
 */
@Component
@ConditionalOnProperty(name = "app.auction.scheduler-enabled", havingValue = "true", matchIfMissing = true)
public class AuctionScheduler {

    private static final Logger log = LoggerFactory.getLogger(AuctionScheduler.class);

    private final AuctionService auctionService;

    public AuctionScheduler(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @Scheduled(fixedDelay = 60_000, initialDelay = 60_000)
    public void syncAuctions() {
        int changed = auctionService.syncDueAuctions();
        if (changed > 0) {
            log.info("Auction scheduler updated {} auction(s)", changed);
        }
    }
}
