package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.OrderItem;

/**
 * Spring Data repository for {@link OrderItem}.
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
