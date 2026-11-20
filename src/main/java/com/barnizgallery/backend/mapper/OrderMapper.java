package com.barnizgallery.backend.mapper;

import java.util.List;

import com.barnizgallery.backend.dto.OrderItemResponse;
import com.barnizgallery.backend.dto.OrderResponse;
import com.barnizgallery.backend.dto.OrderStatusChangeResponse;
import com.barnizgallery.backend.dto.OrderSummaryResponse;
import com.barnizgallery.backend.dto.PaymentObligationResponse;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Order;
import com.barnizgallery.backend.entity.OrderItem;
import com.barnizgallery.backend.entity.OrderStatusHistory;
import com.barnizgallery.backend.entity.PaymentObligation;

/**
 * Converts orders, their lines and their payment obligations to DTOs.
 */
public final class OrderMapper {

    private OrderMapper() {
    }

    public static OrderResponse toResponse(Order order, List<OrderItem> items, List<PaymentObligation> obligations) {
        return new OrderResponse(order.getOrderId(), order.getOrderCode(), order.getOrderType(), order.getStatus(),
                order.getSubtotal(), order.getShippingCost(), order.getTotal(), order.getCurrency(),
                order.getShippingAddress(), order.getPaymentDueAt(), order.getPaidAt(), order.getCreatedAt(),
                items.stream().map(OrderMapper::toResponse).toList(),
                obligations.stream().map(OrderMapper::toResponse).toList());
    }

    public static OrderSummaryResponse toSummary(Order order) {
        return new OrderSummaryResponse(order.getOrderId(), order.getOrderCode(), order.getOrderType(),
                order.getStatus(), order.getTotal(), order.getCurrency(), order.getPaymentDueAt(), order.getPaidAt(),
                order.getCreatedAt());
    }

    public static OrderItemResponse toResponse(OrderItem item) {
        Artwork artwork = item.getArtwork();
        return new OrderItemResponse(item.getOrderItemId(), item.getStore().getStoreId(),
                artwork == null ? null : artwork.getArtworkId(), artwork == null ? null : artwork.getTitleEs(),
                artwork == null ? null : artwork.getTitleEn(), item.getQuantity(), item.getUnitPrice(),
                item.getLineTotal(), item.getFulfillmentStatus(), item.getCarrier(), item.getTrackingNumber());
    }

    public static OrderStatusChangeResponse toResponse(OrderStatusHistory change) {
        return new OrderStatusChangeResponse(change.getOldStatus(), change.getNewStatus(), change.getChangedAt(),
                change.getNote());
    }

    public static PaymentObligationResponse toResponse(PaymentObligation obligation) {
        return new PaymentObligationResponse(obligation.getObligationId(), obligation.getStage(),
                obligation.getAmount(), obligation.getCurrency(), obligation.getStatus(), obligation.getDueAt());
    }
}
