package com.barnizgallery.backend.repository;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.barnizgallery.backend.model.entity.Bid;

/**
 * Spring Data repository for {@link Bid}.
 */
public interface BidRepository extends JpaRepository<Bid, Integer> {

    /** Highest bid amount of an auction, or null if it has no bids. */
    @Query("select max(b.amount) from Bid b where b.auction.auctionId = :auctionId")
    BigDecimal findHighestAmount(Integer auctionId);

    long countByAuctionAuctionId(Integer auctionId);
}
