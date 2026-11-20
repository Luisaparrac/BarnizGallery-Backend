package com.barnizgallery.backend.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.barnizgallery.backend.entity.Order;
import com.barnizgallery.backend.entity.Payment;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.OrderStatus;
import com.barnizgallery.backend.enums.PaymentStatus;
import com.barnizgallery.backend.repository.OrderItemRepository;

import jakarta.persistence.EntityManager;

/**
 * Tells the buyer the result of a payment and, when the order becomes paid, tells each artisan with lines in it.
 * The order is read again first, because the database changed its status in this transaction.
 */
@Component
public class PaymentNotifier {

    static final String PAYMENT_APPROVED = "payment_approved";
    static final String PAYMENT_DECLINED = "payment_declined";
    static final String ORDER_PAID = "order_paid";

    private final NotificationService notificationService;
    private final OrderItemRepository itemRepository;
    private final EntityManager entityManager;

    public PaymentNotifier(NotificationService notificationService, OrderItemRepository itemRepository,
            EntityManager entityManager) {
        this.notificationService = notificationService;
        this.itemRepository = itemRepository;
        this.entityManager = entityManager;
    }

    public void paymentChanged(Payment payment) {
        Order order = payment.getObligation().getOrder();
        entityManager.refresh(order);
        String link = "/orders/" + order.getOrderId();
        if (payment.getStatus() != PaymentStatus.APPROVED) {
            notificationService.notify(order.getBuyer(), PAYMENT_DECLINED, link, null, order.getOrderCode());
            return;
        }
        notificationService.notify(order.getBuyer(), PAYMENT_APPROVED, link, null, order.getOrderCode(),
                payment.getAmount().toPlainString(), payment.getCurrency());
        if (order.getStatus() == OrderStatus.PAID) {
            Map<Long, User> artisans = new LinkedHashMap<>();
            itemRepository.findByOrderOrderIdOrderByOrderItemIdAsc(order.getOrderId()).stream()
                    .map(item -> item.getStore().getMaster().getUser())
                    .filter(Objects::nonNull)
                    .forEach(user -> artisans.putIfAbsent(user.getUserId(), user));
            artisans.values().forEach(artisan -> notificationService.notify(artisan, ORDER_PAID,
                    "/artisan/orders", null, order.getOrderCode()));
        }
    }
}
