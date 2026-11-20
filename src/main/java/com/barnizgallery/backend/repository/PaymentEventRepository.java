package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.PaymentEvent;

/**
 * Spring Data repository for {@link PaymentEvent}.
 */
public interface PaymentEventRepository extends JpaRepository<PaymentEvent, Long> {

    boolean existsByGatewayAndGatewayEventId(String gateway, String gatewayEventId);
}
