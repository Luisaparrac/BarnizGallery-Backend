package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.Payout;

/**
 * Spring Data repository for {@link Payout}.
 */
public interface PayoutRepository extends JpaRepository<Payout, Long> {
}
