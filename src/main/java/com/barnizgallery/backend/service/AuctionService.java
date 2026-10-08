package com.barnizgallery.backend.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.request.CreateAuctionRequest;
import com.barnizgallery.backend.dto.response.AuctionResponse;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.model.entity.Artwork;
import com.barnizgallery.backend.model.entity.Auction;
import com.barnizgallery.backend.model.enums.ArtworkStatus;
import com.barnizgallery.backend.model.enums.AuctionStatus;
import com.barnizgallery.backend.patterns.builder.AuctionBuilder;
import com.barnizgallery.backend.patterns.facade.AiFacade;
import com.barnizgallery.backend.patterns.observer.AuctionEvent;
import com.barnizgallery.backend.patterns.observer.AuctionEventPublisher;
import com.barnizgallery.backend.patterns.state.AuctionContext;
import com.barnizgallery.backend.repository.AuctionRepository;
import com.barnizgallery.backend.repository.BidRepository;

/**
 * Auction lifecycle: creation with the Builder pattern, status changes with the State pattern
 * and notifications with the Observer pattern.
 * <p>
 * Reading an auction first syncs its status with the current time, because Render's free plan
 * sleeps and the scheduler does not run while it sleeps. That is why the read methods here use
 * a read-write transaction.
 */
@Service
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final ArtworkService artworkService;
    private final AiFacade aiFacade;
    private final AuctionResponseAssembler assembler;
    private final AuctionEventPublisher eventPublisher;
    private final Clock clock;

    public AuctionService(AuctionRepository auctionRepository, BidRepository bidRepository,
            ArtworkService artworkService, AiFacade aiFacade, AuctionResponseAssembler assembler,
            AuctionEventPublisher eventPublisher, Clock clock) {
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
        this.artworkService = artworkService;
        this.aiFacade = aiFacade;
        this.assembler = assembler;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Transactional
    public List<AuctionResponse> findAll(AuctionStatus status) {
        syncDueAuctions();
        List<Auction> auctions = status == null
                ? auctionRepository.findAllWithArtwork()
                : auctionRepository.findByStatusWithArtwork(status);
        return assembler.toResponses(auctions);
    }

    @Transactional
    public AuctionResponse findById(Integer auctionId) {
        return assembler.toResponse(getSyncedAuction(auctionId));
    }

    /** Creates an auction with the Builder. If it starts now, the artwork goes to "in auction". */
    @Transactional
    public AuctionResponse create(CreateAuctionRequest request) {
        Artwork artwork = artworkService.getArtwork(request.artworkId());
        AuctionBuilder builder = new AuctionBuilder(auctionRepository, clock)
                .forArtwork(artwork)
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
    public AuctionResponse start(Integer auctionId) {
        AuctionContext context = contextFor(getAuction(auctionId));
        context.start();
        publish(context.getAuction(), context.getStatus());
        return assembler.toResponse(context.getAuction());
    }

    @Transactional
    public AuctionResponse finish(Integer auctionId) {
        AuctionContext context = contextFor(getAuction(auctionId));
        context.finish();
        publish(context.getAuction(), context.getStatus());
        return assembler.toResponse(context.getAuction());
    }

    @Transactional
    public AuctionResponse cancel(Integer auctionId) {
        AuctionContext context = contextFor(getAuction(auctionId));
        context.cancel();
        publish(context.getAuction(), context.getStatus());
        return assembler.toResponse(context.getAuction());
    }

    /**
     * Starts scheduled auctions whose start date arrived and finishes active ones whose end
     * date passed. Called by the scheduler every 60 s and before listing auctions.
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

    /** Loads an auction and brings its status up to date with the current time. */
    @Transactional
    public Auction getSyncedAuction(Integer auctionId) {
        Auction auction = getAuction(auctionId);
        sync(auction, LocalDateTime.now(clock));
        return auction;
    }

    /** Same as {@link #getSyncedAuction} but locks the auction row until the transaction ends. */
    @Transactional
    public Auction getSyncedAuctionForUpdate(Integer auctionId) {
        Auction auction = auctionRepository.findByIdForUpdate(auctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Auction", auctionId));
        sync(auction, LocalDateTime.now(clock));
        return auction;
    }

    /** Wraps the auction in a State pattern context. */
    public AuctionContext contextFor(Auction auction) {
        return new AuctionContext(auction, () -> bidRepository.countByAuctionAuctionId(auction.getAuctionId()));
    }

    private boolean sync(Auction auction, LocalDateTime now) {
        List<AuctionStatus> reached = contextFor(auction).syncWithClock(now);
        reached.forEach(status -> publish(auction, status));
        return !reached.isEmpty();
    }

    /** Translates a reached status into an Observer event. */
    private void publish(Auction auction, AuctionStatus reached) {
        AuctionEvent.Type type = switch (reached) {
            case ACTIVE -> AuctionEvent.Type.AUCTION_STARTED;
            case FINISHED -> AuctionEvent.Type.AUCTION_FINISHED;
            case CANCELLED -> AuctionEvent.Type.AUCTION_CANCELLED;
            case SCHEDULED -> null;
        };
        if (type != null) {
            eventPublisher.publish(AuctionEvent.of(type, auction.getAuctionId(), auction.getArtwork().getArtworkId()));
        }
    }

    private Auction getAuction(Integer auctionId) {
        return auctionRepository.findByIdWithArtwork(auctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Auction", auctionId));
    }
}
