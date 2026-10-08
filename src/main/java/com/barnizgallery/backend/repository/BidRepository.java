package com.barnizgallery.backend.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.barnizgallery.backend.entity.Bid;

/**
 * Spring Data repository for {@link Bid}.
 */
public interface BidRepository extends JpaRepository<Bid, Integer> {

    /** Highest bid amount of an auction, or null if it has no bids. */
    @Query("select max(b.amount) from Bid b where b.auction.auctionId = :auctionId")
    BigDecimal findHighestAmount(Integer auctionId);

    long countByAuctionAuctionId(Integer auctionId);

    /** Bids of a visitor after a moment (used to detect bid flooding). */
    long countByVisitorVisitorIdAndBidDateAfter(Integer visitorId, LocalDateTime since);

    /** Current highest bid of an auction (the earliest one wins a tie). */
    Optional<Bid> findFirstByAuctionAuctionIdOrderByAmountDescBidDateAsc(Integer auctionId);

    /** Bids of an auction with their visitor, highest first. */
    @Query("select b from Bid b join fetch b.visitor where b.auction.auctionId = :auctionId "
            + "order by b.amount desc, b.bidDate asc")
    List<Bid> findByAuctionWithVisitor(Integer auctionId);

    /** Highest bid and bid count of many auctions in a single query. */
    @Query("select b.auction.auctionId as auctionId, max(b.amount) as highest, count(b) as bidCount "
            + "from Bid b where b.auction.auctionId in :auctionIds group by b.auction.auctionId")
    List<AuctionBidStats> findStats(Collection<Integer> auctionIds);
}
