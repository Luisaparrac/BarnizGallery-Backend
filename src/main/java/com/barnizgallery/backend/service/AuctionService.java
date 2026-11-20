package com.barnizgallery.backend.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.AuctionResponse;
import com.barnizgallery.backend.dto.CreateAuctionRequest;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Auction;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.AuctionStatus;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.patterns.builder.AuctionBuilder;
import com.barnizgallery.backend.patterns.facade.AiFacade;
import com.barnizgallery.backend.patterns.observer.AuctionEvent;
import com.barnizgallery.backend.patterns.observer.AuctionEventPublisher;
import com.barnizgallery.backend.patterns.observer.AuctionEventType;
import com.barnizgallery.backend.patterns.state.AuctionContext;
import com.barnizgallery.backend.repository.AuctionRepository;
import com.barnizgallery.backend.repository.BidRepository;

/**
 * Auction lifecycle: creation with the Builder pattern, status changes with the State pattern
 * and notifications with the Observer pattern.
 * <p>
 * Read requests never write: auction statuses are synced with the clock only by the scheduler and by
 * write actions (bid, start, finish, cancel). When the service wakes up after sleeping, the scheduler
 * catches up on its next run. Historical auctions without currency are never changed automatically.
 */
@Service
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final ArtworkService artworkService;
    private final AiFacade aiFacade;
    private final AuctionResponseAssembler assembler;
    private final AuctionEventPublisher eventPublisher;
    private final AppSettingsService settings;
    private final Clock clock;

    public AuctionService(AuctionRepository auctionRepository, BidRepository bidRepository,
            ArtworkService artworkService, AiFacade aiFacade, AuctionResponseAssembler assembler,
            AuctionEventPublisher eventPublisher, AppSettingsService settings, Clock clock) {
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
        this.artworkService = artworkService;
        this.aiFacade = aiFacade;
        this.assembler = assembler;
        this.eventPublisher = eventPublisher;
        this.settings = settings;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<AuctionResponse> findAll(AuctionStatus status) {
        List<Auction> auctions = status == null
                ? auctionRepository.findAllWithArtwork()
                : auctionRepository.findByStatusWithArtwork(status);
        return assembler.toResponses(auctions);
    }

    @Transactional(readOnly = true)
    public AuctionResponse findById(Long auctionId) {
        return assembler.toResponse(getAuction(auctionId));
    }

    /**
     * Creates an auction with the Builder, in the default currency of {@code app_settings}. If it starts now, the artwork
     * goes to "in auction".
     */
    @Transactional
    public AuctionResponse create(CreateAuctionRequest request) {
        Artwork artwork = artworkService.getArtwork(request.artworkId());
        ActiveMasterRule.requireActive(artwork.getStore().getMaster());
        AuctionBuilder builder = new AuctionBuilder(auctionRepository, clock)
                .forArtwork(artwork)
                .currency(settings.text(AppSettingsService.CURRENCY_DEFAULT))
                .startsAt(request.startDate())
                .endsAt(request.endDate());
        if (request.basePrice() != null) {
            builder.basePrice(request.basePrice());
        } else {
            builder.useSuggestedBasePrice(aiFacade);
        }
        Auction auction = builder.build();
        if (auction.getStatus() == AuctionStatus.ACTIVE) {
            artwork.setStatus(ArtworkStatus.IN_AUCTION);
        }
        Auction saved = auctionRepository.save(auction);
        if (saved.getStatus() == AuctionStatus.ACTIVE) {
            publish(saved, AuctionStatus.ACTIVE);
        }
        return assembler.toResponse(saved);
    }

    @Transactional
    public AuctionResponse start(Long auctionId) {
        AuctionContext context = contextFor(getSyncedAuctionForUpdate(auctionId));
        context.start();
        publish(context.getAuction(), context.getStatus());
        return assembler.toResponse(context.getAuction());
    }

    @Transactional
    public AuctionResponse finish(Long auctionId) {
        AuctionContext context = contextFor(getSyncedAuctionForUpdate(auctionId));
        context.finish();
        publish(context.getAuction(), context.getStatus());
        return assembler.toResponse(context.getAuction());
    }

    @Transactional
    public AuctionResponse cancel(Long auctionId) {
        AuctionContext context = contextFor(getSyncedAuctionForUpdate(auctionId));
        context.cancel();
        publish(context.getAuction(), context.getStatus());
        return assembler.toResponse(context.getAuction());
    }

    /**
     * Starts scheduled auctions whose start date arrived and finishes active ones whose end
     * date passed. Called by the scheduler every 60 s.
     *
     * @return number of auctions that changed
     */
    @Transactional
    public int syncDueAuctions() {
        LocalDateTime now = LocalDateTime.now(clock);
        int changed = 0;
        for (Auction auction : auctionRepository.findDueForTransition(now)) {
            if (sync(auction, now)) {
                changed++;
            }
        }
        return changed;
    }

    /** Fails with 404 if the auction does not exist. Never changes it. */
    @Transactional(readOnly = true)
    public void requireExists(Long auctionId) {
        if (!auctionRepository.existsById(auctionId)) {
            throw new ResourceNotFoundException(ErrorCode.AUCTION_NOT_FOUND, auctionId);
        }
    }

    /**
     * Loads an auction for a write action: locks its row until the transaction ends and brings its status up
     * to date with the current time.
     */
    @Transactional
    public Auction getSyncedAuctionForUpdate(Long auctionId) {
        Auction auction = auctionRepository.findByIdForUpdate(auctionId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.AUCTION_NOT_FOUND, auctionId));
        sync(auction, LocalDateTime.now(clock));
        return auction;
    }

    /** Wraps the auction in a State pattern context. */
    public AuctionContext contextFor(Auction auction) {
        return new AuctionContext(auction, () -> bidRepository.countByAuctionAuctionId(auction.getAuctionId()));
    }

    /** Historical auctions without currency are frozen: no automatic transition changes them. */
    private boolean sync(Auction auction, LocalDateTime now) {
        if (auction.getCurrency() == null) {
            return false;
        }
        List<AuctionStatus> reached = contextFor(auction).syncWithClock(now);
        reached.forEach(status -> publish(auction, status));
        return !reached.isEmpty();
    }

    /** Translates a reached status into an Observer event. */
    private void publish(Auction auction, AuctionStatus reached) {
        AuctionEventType type = null;
        if (reached == AuctionStatus.ACTIVE) {
            type = AuctionEventType.AUCTION_STARTED;
        } else if (reached == AuctionStatus.FINISHED) {
            type = AuctionEventType.AUCTION_FINISHED;
        } else if (reached == AuctionStatus.CANCELLED) {
            type = AuctionEventType.AUCTION_CANCELLED;
        }
        if (type != null) {
            eventPublisher.publish(AuctionEvent.of(type, auction.getAuctionId(), auction.getArtwork().getArtworkId()));
        }
    }

    private Auction getAuction(Long auctionId) {
        return auctionRepository.findByIdWithArtwork(auctionId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.AUCTION_NOT_FOUND, auctionId));
    }
}
