package com.barnizgallery.backend.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.barnizgallery.backend.model.entity.Auction;
import com.barnizgallery.backend.model.enums.AuctionStatus;

import jakarta.persistence.LockModeType;

/**
 * Spring Data repository for {@link Auction}.
 */
public interface AuctionRepository extends JpaRepository<Auction, Integer> {

    /** Most recent auction of an artwork among the given statuses (for example scheduled or active). */
    Optional<Auction> findFirstByArtworkArtworkIdAndStatusInOrderByStartDateDesc(Integer artworkId,
            Collection<AuctionStatus> statuses);

    boolean existsByArtworkArtworkIdAndStatusIn(Integer artworkId, Collection<AuctionStatus> statuses);

    @Query("select a from Auction a join fetch a.artwork order by a.startDate desc, a.auctionId desc")
    List<Auction> findAllWithArtwork();

    @Query("select a from Auction a join fetch a.artwork where a.status = :status "
            + "order by a.startDate desc, a.auctionId desc")
    List<Auction> findByStatusWithArtwork(AuctionStatus status);

    @Query("select a from Auction a join fetch a.artwork where a.auctionId = :auctionId")
    Optional<Auction> findByIdWithArtwork(Integer auctionId);

    /**
     * Loads the auction with a row lock (SELECT ... FOR UPDATE) so two bids on the same
     * auction are processed one after the other.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Auction a join fetch a.artwork where a.auctionId = :auctionId")
    Optional<Auction> findByIdForUpdate(Integer auctionId);

    /** Scheduled auctions whose start date has arrived, or active ones whose end date has passed. */
    @Query("select a from Auction a join fetch a.artwork where "
            + "(a.status = com.barnizgallery.backend.model.enums.AuctionStatus.SCHEDULED and a.startDate <= :now) "
            + "or (a.status = com.barnizgallery.backend.model.enums.AuctionStatus.ACTIVE and a.endDate <= :now)")
    List<Auction> findDueForTransition(LocalDateTime now);
}
