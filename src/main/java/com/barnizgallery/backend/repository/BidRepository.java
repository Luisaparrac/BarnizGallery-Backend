package com.barnizgallery.backend.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.barnizgallery.backend.model.entity.Bid;

/**
 * Spring Data repository for {@link Bid}.
 */
public interface BidRepository extends JpaRepository<Bid, Integer> {

    /** Highest bid and number of bids of one auction. */
    interface AuctionBidStats {
        Integer getAuctionId();

        BigDecimal getHighest();

        long getBidCount();
    }

    /** Highest bid amount of an auction, or null if it has no bids. */
    @Query("select max(b.amount) from Bid b where b.auction.auctionId = :auctionId")
    BigDecimal findHighestAmount(Integer auctionId);

    long countByAuctionAuctionId(Integer auctionId);

    /** Bids of a visitor after a moment (used to detect bid flooding). */
    long countByVisitorVisitorIdAndBidDateAfter(Integer visitorId, LocalDateTime since);

    /** Highest bid and bid count of many auctions in a single query. */
    @Query("select b.auction.auctionId as auctionId, max(b.amount) as highest, count(b) as bidCount "
            + "from Bid b where b.auction.auctionId in :auctionIds group by b.auction.auctionId")
    List<AuctionBidStats> findStats(Collection<Integer> auctionIds);
}
