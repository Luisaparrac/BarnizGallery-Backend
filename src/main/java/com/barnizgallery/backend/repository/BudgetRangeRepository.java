package com.barnizgallery.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.BudgetRange;

/**
 * Spring Data repository for {@link BudgetRange}.
 */
public interface BudgetRangeRepository extends JpaRepository<BudgetRange, Long> {

    Optional<BudgetRange> findByCode(String code);

    List<BudgetRange> findAllByOrderByMinAmountAscCodeAsc();
}
