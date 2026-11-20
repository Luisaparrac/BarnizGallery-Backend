package com.barnizgallery.backend.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.barnizgallery.backend.entity.OrderItem;
import com.barnizgallery.backend.enums.OrderStatus;

/**
 * Spring Data repository for {@link OrderItem}.
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderOrderIdOrderByOrderItemIdAsc(Long orderId);

    /** Lines of a store whose order has one of the statuses, newest order first. */
    @Query("select i from OrderItem i join fetch i.order o where i.store.storeId = :storeId and o.status in :statuses "
            + "order by o.orderId desc, i.orderItemId")
    List<OrderItem> findByStoreAndOrderStatus(Long storeId, Collection<OrderStatus> statuses);

    /**
     * Reserves the artwork of a line until {@code expiresAt} through the database function
     * {@code museo.reserve_order_item}, which locks the artwork and refuses missing or insufficient stock.
     */
    @Query(value = "SELECT museo.reserve_order_item(:orderItemId, :expiresAt)", nativeQuery = true)
    Long reserve(Long orderItemId, Instant expiresAt);
}
