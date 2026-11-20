package com.barnizgallery.backend.repository;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.barnizgallery.backend.entity.OrderItem;

/**
 * Spring Data repository for {@link OrderItem}.
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Reserves the artwork of a line until {@code expiresAt} through the database function
     * {@code museo.reserve_order_item}, which locks the artwork and refuses missing or insufficient stock.
     */
    @Query(value = "SELECT museo.reserve_order_item(:orderItemId, :expiresAt)", nativeQuery = true)
    Long reserve(Long orderItemId, Instant expiresAt);
}
