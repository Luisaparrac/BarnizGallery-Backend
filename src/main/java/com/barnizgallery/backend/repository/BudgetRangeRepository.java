package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.BudgetRange;

/**
 * Spring Data repository for {@link BudgetRange}.
 */
public interface BudgetRangeRepository extends JpaRepository<BudgetRange, Long> {
}
