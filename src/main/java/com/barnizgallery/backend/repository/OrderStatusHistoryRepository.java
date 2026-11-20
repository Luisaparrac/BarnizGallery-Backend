package com.barnizgallery.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.OrderStatusHistory;

/**
 * Spring Data repository for {@link OrderStatusHistory}.
 */
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {

    List<OrderStatusHistory> findByOrderOrderIdOrderByChangedAtAscHistoryIdAsc(Long orderId);
}
