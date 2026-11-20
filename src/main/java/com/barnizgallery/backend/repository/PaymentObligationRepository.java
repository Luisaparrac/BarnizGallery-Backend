package com.barnizgallery.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.PaymentObligation;

/**
 * Spring Data repository for {@link PaymentObligation}.
 */
public interface PaymentObligationRepository extends JpaRepository<PaymentObligation, Long> {

    List<PaymentObligation> findByOrderOrderIdOrderByObligationIdAsc(Long orderId);
}
