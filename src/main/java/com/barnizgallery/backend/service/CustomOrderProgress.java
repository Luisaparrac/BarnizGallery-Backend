package com.barnizgallery.backend.service;

import org.springframework.stereotype.Component;

import com.barnizgallery.backend.entity.CustomRequest;
import com.barnizgallery.backend.entity.Payment;
import com.barnizgallery.backend.enums.CustomRequestStatus;
import com.barnizgallery.backend.enums.OrderType;
import com.barnizgallery.backend.enums.PaymentObligationStage;
import com.barnizgallery.backend.enums.PaymentStatus;
import com.barnizgallery.backend.repository.OrderItemRepository;

/**
 * Moves a custom request when a payment of its {@code encargo} order is approved: the deposit puts it in production
 * and the balance completes it.
 */
@Component
public class CustomOrderProgress {

    private final OrderItemRepository itemRepository;

    public CustomOrderProgress(OrderItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public void paymentChanged(Payment payment) {
        var order = payment.getObligation().getOrder();
        if (payment.getStatus() != PaymentStatus.APPROVED || order.getOrderType() != OrderType.CUSTOM) {
            return;
        }
        itemRepository.findByOrderOrderIdOrderByOrderItemIdAsc(order.getOrderId()).stream()
                .filter(item -> item.getCustomOffer() != null)
                .map(item -> item.getCustomOffer().getCustomRequest())
                .forEach(request -> advance(request, payment.getObligation().getStage()));
    }

    private static void advance(CustomRequest request, PaymentObligationStage stage) {
        if (stage == PaymentObligationStage.DEPOSIT && request.getStatus() == CustomRequestStatus.ACCEPTED) {
            request.setStatus(CustomRequestStatus.IN_PRODUCTION);
        } else if (stage == PaymentObligationStage.BALANCE && request.getStatus() != CustomRequestStatus.COMPLETED) {
            request.setStatus(CustomRequestStatus.COMPLETED);
        }
    }
}
