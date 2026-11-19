package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.OrderStatusHistory;

/**
 * Spring Data repository for {@link OrderStatusHistory}.
 */
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {
}
