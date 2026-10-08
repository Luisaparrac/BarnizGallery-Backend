package com.barnizgallery.backend.repository;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.model.entity.Auction;
import com.barnizgallery.backend.model.enums.AuctionStatus;

/**
 * Spring Data repository for {@link Auction}.
 */
public interface AuctionRepository extends JpaRepository<Auction, Integer> {

    /** Most recent auction of an artwork among the given statuses (for example scheduled or active). */
    Optional<Auction> findFirstByArtworkArtworkIdAndStatusInOrderByStartDateDesc(Integer artworkId,
            Collection<AuctionStatus> statuses);
}
