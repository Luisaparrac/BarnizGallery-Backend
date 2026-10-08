package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.model.entity.Bid;

/**
 * Spring Data repository for {@link Bid}.
 */
public interface BidRepository extends JpaRepository<Bid, Integer> {
}
