package com.barnizgallery.backend.entity;

import java.time.Instant;

import org.hibernate.annotations.Immutable;

import com.barnizgallery.backend.converter.OrderStatusConverter;
import com.barnizgallery.backend.enums.OrderStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Status change of an order, written only by a database trigger (read-only here).
 * Maps the table {@code museo.order_status_history}.
 */
@Entity
@Immutable
@Table(name = "order_status_history")
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Convert(converter = OrderStatusConverter.class)
    @Column(name = "old_status", length = 30)
    private OrderStatus oldStatus;

    @Convert(converter = OrderStatusConverter.class)
    @Column(name = "new_status", nullable = false, length = 30)
    private OrderStatus newStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private User changedBy;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    @Column(name = "note")
    private String note;

    public Long getHistoryId() {
        return historyId;
    }

    public Order getOrder() {
        return order;
    }

    public OrderStatus getOldStatus() {
        return oldStatus;
    }

    public OrderStatus getNewStatus() {
        return newStatus;
    }

    public User getChangedBy() {
        return changedBy;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public String getNote() {
        return note;
    }
}
