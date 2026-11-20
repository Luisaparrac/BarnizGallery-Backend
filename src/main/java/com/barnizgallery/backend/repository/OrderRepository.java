package com.barnizgallery.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.barnizgallery.backend.entity.Order;

/**
 * Spring Data repository for {@link Order}.
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    boolean existsByOrderCode(String orderCode);

    /**
     * Frees the active reservations of a cancelled order through the database function
     * {@code museo.release_reservations_for_order}.
     */
    @Query(value = "SELECT CAST(museo.release_reservations_for_order(:orderId, 'liberada') AS text)", nativeQuery = true)
    String releaseReservations(Long orderId);

    List<Order> findByBuyerUserIdOrderByOrderIdDesc(Long buyerId);
}
