package com.barnizgallery.backend.mapper;

import java.util.List;

import com.barnizgallery.backend.dto.ArtisanOrderLineResponse;
import com.barnizgallery.backend.dto.OrderItemResponse;
import com.barnizgallery.backend.dto.OrderResponse;
import com.barnizgallery.backend.dto.OrderStatusChangeResponse;
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
        return toResponse(order, items, obligations, List.of());
    }

    /** The order with its lines, obligations and status changes (frontend names included). */
    public static OrderResponse toResponse(Order order, List<OrderItem> items, List<PaymentObligation> obligations,
            List<OrderStatusChangeResponse> history) {
        return new OrderResponse(order.getOrderId(), order.getOrderCode(), order.getOrderType(), order.getStatus(),
                order.getSubtotal(), order.getShippingCost(), order.getTotal(), order.getCurrency(),
                order.getShippingAddress(), order.getPaymentDueAt(), order.getPaidAt(), order.getCreatedAt(),
                items.stream().map(OrderMapper::toResponse).toList(),
                obligations.stream().map(OrderMapper::toResponse).toList(), order.getOrderId(), order.getOrderCode(),
                order.getOrderType(), order.getBuyer().getUserId(), order.getBuyer().getName(), order.getPlatformFee(),
                null, history);
    }

    public static ArtisanOrderLineResponse toArtisanLine(OrderItem item) {
        Order order = item.getOrder();
        Artwork artwork = item.getArtwork();
        return new ArtisanOrderLineResponse(item.getOrderItemId(), order.getOrderId(), order.getOrderCode(),
                order.getStatus(), order.getPaidAt(), artwork == null ? null : artwork.getArtworkId(),
                artwork == null ? null : artwork.getTitleEs(), artwork == null ? null : artwork.getTitleEn(),
                item.getQuantity(), item.getLineTotal(), item.getArtisanNetAmount(), item.getCurrency(),
                item.getFulfillmentStatus(), item.getCarrier(), item.getTrackingNumber(), order.getShippingAddress());
    }

    public static OrderItemResponse toResponse(OrderItem item) {
        Artwork artwork = item.getArtwork();
        return new OrderItemResponse(item.getOrderItemId(), item.getStore().getStoreId(),
                artwork == null ? null : artwork.getArtworkId(), artwork == null ? null : artwork.getTitleEs(),
                artwork == null ? null : artwork.getTitleEn(), item.getQuantity(), item.getUnitPrice(),
                item.getLineTotal(), item.getFulfillmentStatus(), item.getCarrier(), item.getTrackingNumber(),
                item.getOrderItemId(), item.getOrder().getOrderId(), item.getStore().getNameEs(),
                item.getAuction() == null ? null : item.getAuction().getAuctionId(),
                item.getCustomOffer() == null ? null : item.getCustomOffer().getCustomOfferId(),
                artwork == null ? null : artwork.getTitleEs(), item.getArtisanNetAmount());
    }

    public static OrderStatusChangeResponse toResponse(OrderStatusHistory change) {
        return new OrderStatusChangeResponse(change.getOldStatus(), change.getNewStatus(), change.getChangedAt(),
                change.getNote(), change.getOldStatus(), change.getNewStatus());
    }

    public static PaymentObligationResponse toResponse(PaymentObligation obligation) {
        return new PaymentObligationResponse(obligation.getObligationId(), obligation.getStage(),
                obligation.getAmount(), obligation.getCurrency(), obligation.getStatus(), obligation.getDueAt());
    }
}
