package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.Payment;
import com.barnizgallery.backend.enums.PaymentStatus;

/**
 * Spring Data repository for {@link Payment}.
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByObligationObligationIdAndStatus(Long obligationId, PaymentStatus status);
}
