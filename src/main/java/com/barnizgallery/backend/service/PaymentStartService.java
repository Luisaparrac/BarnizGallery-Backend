package com.barnizgallery.backend.service;

import java.time.Clock;
import java.util.EnumSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.PaymentStartResponse;
import com.barnizgallery.backend.dto.StartPaymentRequest;
import com.barnizgallery.backend.entity.Order;
import com.barnizgallery.backend.entity.Payment;
import com.barnizgallery.backend.entity.PaymentObligation;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.enums.OrderStatus;
import com.barnizgallery.backend.enums.PaymentObligationStatus;
import com.barnizgallery.backend.enums.PaymentStatus;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.patterns.adapter.CheckoutRequest;
import com.barnizgallery.backend.patterns.adapter.CheckoutSession;
import com.barnizgallery.backend.patterns.adapter.PaymentGateway;
import com.barnizgallery.backend.repository.PaymentObligationRepository;
import com.barnizgallery.backend.repository.PaymentRepository;

/**
 * Starts the payment of one obligation of an own order. Payments go per obligation: the single payment of a
 * purchase, or the deposit and the balance of a custom order. The gateway is chosen by the obligation currency
 * (COP → Wompi, USD → PayPal); the simulated gateway is used only when asked for and while
 * {@code payments.test_mode} is true.
 */
@Service
public class PaymentStartService {

    private static final Set<OrderStatus> CLOSED = EnumSet.of(OrderStatus.CANCELLED, OrderStatus.REFUNDED);

    private final OrderQueryService orderQueryService;
    private final PaymentObligationRepository obligationRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayRegistry gatewayRegistry;
    private final PaymentSettings paymentSettings;
    private final Clock clock;
    private final String frontendUrl;

    public PaymentStartService(OrderQueryService orderQueryService, PaymentObligationRepository obligationRepository,
            PaymentRepository paymentRepository, PaymentGatewayRegistry gatewayRegistry,
            PaymentSettings paymentSettings, Clock clock, @Value("${app.frontend-url}") String frontendUrl) {
        this.orderQueryService = orderQueryService;
        this.obligationRepository = obligationRepository;
        this.paymentRepository = paymentRepository;
        this.gatewayRegistry = gatewayRegistry;
        this.paymentSettings = paymentSettings;
        this.clock = clock;
        this.frontendUrl = frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1)
                : frontendUrl;
    }

    @Transactional
    public PaymentStartResponse start(Long orderId, StartPaymentRequest request) {
        Order order = orderQueryService.myOrder(orderId);
        PaymentObligation obligation = obligationToPay(order, request.obligationId());
        if (obligation.getDueAt() != null && !obligation.getDueAt().isAfter(clock.instant())) {
            throw new BusinessRuleException(ErrorCode.PAYMENT_DEADLINE_PASSED, order.getOrderCode());
        }
        if (paymentRepository.existsByObligationObligationIdAndStatus(obligation.getObligationId(),
                PaymentStatus.PENDING)) {
            throw new BusinessRuleException(ErrorCode.PAYMENT_ALREADY_PENDING, order.getOrderCode());
        }
        PaymentGateway gateway = Boolean.TRUE.equals(request.simulated()) ? simulatedGateway()
                : gatewayRegistry.forCurrency(obligation.getCurrency());
        Payment payment = new Payment();
        payment.setObligation(obligation);
        payment.setGateway(gateway.code());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setAmount(obligation.getAmount());
        payment.setCurrency(obligation.getCurrency());
        paymentRepository.save(payment);
        String reference = PaymentReferences.of(payment.getPaymentId());
        String orderUrl = frontendUrl + "/orders/" + orderId;
        CheckoutSession session = gateway.startCheckout(new CheckoutRequest(reference, payment.getAmount(),
                payment.getCurrency(), order.getBuyer().getEmail(), "MUSEO " + order.getOrderCode(), orderUrl,
                orderUrl + "?payment=cancelled", obligation.getDueAt()));
        payment.setGatewayReference(session.gatewayReference());
        return new PaymentStartResponse(payment.getPaymentId(), obligation.getObligationId(), gateway.code(),
                reference, session.redirectUrl(), payment.getAmount(), payment.getCurrency(), payment.getStatus(),
                payment.getPaymentId(), orderId, session.redirectUrl());
    }

    private PaymentObligation obligationToPay(Order order, Long obligationId) {
        if (CLOSED.contains(order.getStatus())) {
            throw new BusinessRuleException(ErrorCode.NOTHING_TO_PAY, order.getOrderCode());
        }
        return obligationRepository.findByOrderOrderIdOrderByObligationIdAsc(order.getOrderId()).stream()
                .filter(o -> obligationId == null || o.getObligationId().equals(obligationId))
                .filter(o -> o.getStatus() == PaymentObligationStatus.PENDING)
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException(ErrorCode.NOTHING_TO_PAY, order.getOrderCode()));
    }

    private PaymentGateway simulatedGateway() {
        if (!paymentSettings.testMode()) {
            throw new BusinessRuleException(ErrorCode.SIMULATED_PAYMENTS_DISABLED);
        }
        return gatewayRegistry.simulated();
    }
}
