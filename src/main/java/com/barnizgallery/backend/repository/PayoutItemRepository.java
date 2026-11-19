package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.PayoutItem;

/**
 * Spring Data repository for {@link PayoutItem}.
 */
public interface PayoutItemRepository extends JpaRepository<PayoutItem, Long> {
}
