package com.barnizgallery.backend.patterns.builder;

import java.util.List;

import com.barnizgallery.backend.entity.Order;
import com.barnizgallery.backend.entity.OrderItem;
import com.barnizgallery.backend.entity.PaymentObligation;

/**
 * <b>Builder pattern – Product.</b> The order made by {@link OrderBuilder}: the order, its lines and its payment
 * obligations, linked together and ready to be saved in that order.
 */
public record BuiltOrder(Order order, List<OrderItem> items, List<PaymentObligation> obligations) {
}
