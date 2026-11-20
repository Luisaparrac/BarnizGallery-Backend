package com.barnizgallery.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.barnizgallery.backend.entity.Payout;

/**
 * Spring Data repository for {@link Payout}.
 */
public interface PayoutRepository extends JpaRepository<Payout, Long> {

    @Query("select p from Payout p join fetch p.master order by p.payoutId desc")
    List<Payout> findAllWithMaster();
}
