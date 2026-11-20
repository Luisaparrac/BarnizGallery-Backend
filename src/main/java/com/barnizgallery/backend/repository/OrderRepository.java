package com.barnizgallery.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.Order;

/**
 * Spring Data repository for {@link Order}.
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    boolean existsByOrderCode(String orderCode);

    List<Order> findByBuyerUserIdOrderByOrderIdDesc(Long buyerId);
}
