package com.barnizgallery.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.OrderResponse;
import com.barnizgallery.backend.entity.Order;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.enums.OrderStatus;
import com.barnizgallery.backend.enums.PaymentObligationStatus;
import com.barnizgallery.backend.enums.PaymentStatus;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.repository.OrderRepository;
import com.barnizgallery.backend.repository.PaymentObligationRepository;
import com.barnizgallery.backend.repository.PaymentRepository;

/**
 * The buyer cancels an own order that is still waiting for payment and has no payment in progress: the order and
 * its pending obligations are cancelled and the database releases the reserved stock. A paid order cannot be
 * cancelled here (refunds are out of scope).
 */
@Service
public class OrderCancelService {

    private final OrderQueryService orderQueryService;
    private final OrderRepository orderRepository;
    private final PaymentObligationRepository obligationRepository;
    private final PaymentRepository paymentRepository;

    public OrderCancelService(OrderQueryService orderQueryService, OrderRepository orderRepository,
            PaymentObligationRepository obligationRepository, PaymentRepository paymentRepository) {
        this.orderQueryService = orderQueryService;
        this.orderRepository = orderRepository;
        this.obligationRepository = obligationRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public OrderResponse cancel(Long orderId) {
        Order order = orderQueryService.myOrder(orderId);
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BusinessRuleException(ErrorCode.ORDER_NOT_CANCELLABLE, order.getOrderCode());
        }
        var obligations = obligationRepository.findByOrderOrderIdOrderByObligationIdAsc(orderId);
        for (var obligation : obligations) {
            if (paymentRepository.existsByObligationObligationIdAndStatus(obligation.getObligationId(),
                    PaymentStatus.PENDING)) {
                throw new BusinessRuleException(ErrorCode.PAYMENT_ALREADY_PENDING, order.getOrderCode());
            }
            if (obligation.getStatus() == PaymentObligationStatus.PENDING) {
                obligation.setStatus(PaymentObligationStatus.CANCELLED);
            }
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.saveAndFlush(order);
        orderRepository.releaseReservations(orderId);
        return orderQueryService.mine(orderId);
    }
}
