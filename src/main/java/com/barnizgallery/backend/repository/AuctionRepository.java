package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.model.entity.Auction;

/**
 * Spring Data repository for {@link Auction}.
 */
public interface AuctionRepository extends JpaRepository<Auction, Integer> {
}
