package com.barnizgallery.backend.dto;

import java.time.Instant;

import com.barnizgallery.backend.enums.OrderStatus;

/**
 * A change of the status of an order, as the database recorded it.
 */
public record OrderStatusChangeResponse(OrderStatus oldStatus, OrderStatus newStatus, Instant changedAt, String note) {
}
