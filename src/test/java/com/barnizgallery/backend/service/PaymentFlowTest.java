package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.orm.jpa.JpaSystemException;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.dto.StartPaymentRequest;
import com.barnizgallery.backend.entity.Order;
import com.barnizgallery.backend.entity.Payment;
import com.barnizgallery.backend.entity.PaymentObligation;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.enums.OrderStatus;
import com.barnizgallery.backend.enums.PaymentObligationStage;
import com.barnizgallery.backend.enums.PaymentObligationStatus;
import com.barnizgallery.backend.enums.PaymentStatus;
import com.barnizgallery.backend.patterns.adapter.CheckoutSession;
import com.barnizgallery.backend.patterns.adapter.GatewayEvent;
import com.barnizgallery.backend.patterns.adapter.PaymentGateway;
import com.barnizgallery.backend.repository.PaymentEventRepository;
import com.barnizgallery.backend.repository.PaymentObligationRepository;
import com.barnizgallery.backend.repository.PaymentRepository;

/** Starting payments per obligation, idempotent webhook events and the result of each payment. */
class PaymentFlowTest {

    private static final Instant NOW = Instant.parse("2026-11-20T12:00:00Z");

    private final PaymentRepository payments = mock(PaymentRepository.class);
    private final PaymentEventRepository events = mock(PaymentEventRepository.class);
    private final PaymentObligationRepository obligations = mock(PaymentObligationRepository.class);
    private final OrderQueryService orders = mock(OrderQueryService.class);
    private final PaymentGatewayRegistry registry = mock(PaymentGatewayRegistry.class);
    private final PaymentSettings settings = mock(PaymentSettings.class);
    private final PaymentNotifier notifier = mock(PaymentNotifier.class);
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
    private final PaymentStartService starter = new PaymentStartService(orders, obligations, payments, registry,
            settings, clock, "https://front.test/");
    private final PaymentWebhookService webhooks = new PaymentWebhookService(registry, events, payments, clock,
            new PaymentResultApplier(payments), notifier,
            mock(CustomOrderProgress.class));

    private final Order order = new Order();
    private final PaymentObligation obligation = new PaymentObligation();

    @BeforeEach
    void pendingOrder() {
        order.setOrderId(1L);
        order.setOrderCode("MUS-TEST");
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setBuyer(TestEntities.user(4, Language.ES));
        obligation.setObligationId(10L);
        obligation.setOrder(order);
        obligation.setStage(PaymentObligationStage.SINGLE_PAYMENT);
        obligation.setStatus(PaymentObligationStatus.PENDING);
        obligation.setAmount(new BigDecimal("245.05"));
        obligation.setCurrency("USD");
        obligation.setDueAt(NOW.plusSeconds(600));
        when(orders.myOrder(1L)).thenReturn(order);
        when(obligations.findByOrderOrderIdOrderByObligationIdAsc(1L)).thenReturn(List.of(obligation));
    }

    @Test
    void aPaymentUsesTheGatewayOfItsCurrencyAndSendsItsReference() {
        PaymentGateway paypal = mock(PaymentGateway.class);
        when(paypal.code()).thenReturn("paypal");
        when(registry.forCurrency("USD")).thenReturn(paypal);
        when(payments.save(any(Payment.class))).thenAnswer(inv -> {
            Payment saved = inv.getArgument(0);
            saved.setPaymentId(7L);
            return saved;
        });
        when(paypal.startCheckout(any())).thenReturn(new CheckoutSession("paypal", "PP-ORDER", "https://pay.test"));

        var started = starter.start(1L, new StartPaymentRequest(null, false));

        assertThat(started.reference()).isEqualTo("PAY-7");
        assertThat(started.amount()).isEqualByComparingTo("245.05");
        assertThat(started.redirectUrl()).isEqualTo("https://pay.test");
    }

    @Test
    void simulatedPaymentsNeedTestModeAndPaymentsRespectTheDeadline() {
        when(settings.testMode()).thenReturn(false);
        assertThatThrownBy(() -> starter.start(1L, new StartPaymentRequest(null, true)))
                .hasMessage("simulated_payments_disabled");

        obligation.setDueAt(NOW);
        assertThatThrownBy(() -> starter.start(1L, new StartPaymentRequest(null, true)))
                .hasMessage("payment_deadline_passed [MUS-TEST]");

        obligation.setDueAt(null);
        when(payments.existsByObligationObligationIdAndStatus(10L, PaymentStatus.PENDING)).thenReturn(true);
        assertThatThrownBy(() -> starter.start(1L, new StartPaymentRequest(null, true)))
                .hasMessage("payment_already_pending [MUS-TEST]");

        obligation.setStatus(PaymentObligationStatus.PAID);
        assertThatThrownBy(() -> starter.start(1L, new StartPaymentRequest(null, false)))
                .hasMessage("nothing_to_pay [MUS-TEST]");
    }

    @Test
    void anApprovedEventIsSavedAppliedAndNotifiedOnce() {
        Payment payment = pendingPayment();
        GatewayEvent event = event("evt-1", PaymentStatus.APPROVED);

        assertThat(webhooks.process(event)).isTrue();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(payment.getGatewayTransactionId()).isEqualTo("tx-1");
        verify(payments).saveAndFlush(payment);
        verify(notifier).paymentChanged(payment);

        when(events.existsByGatewayAndGatewayEventId("wompi", "evt-1")).thenReturn(true);
        assertThat(webhooks.process(event)).isFalse();
    }

    @Test
    void lateEventsDoNotChangeAFinishedPaymentAndUnknownReferencesAre404() {
        Payment payment = pendingPayment();
        payment.setStatus(PaymentStatus.REJECTED);

        webhooks.process(event("evt-2", PaymentStatus.APPROVED));

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REJECTED);
        verify(notifier, never()).paymentChanged(any());
        assertThatThrownBy(() -> webhooks.process(new GatewayEvent("wompi", "evt-3", "OTHER-1", "tx", PaymentStatus.APPROVED,
                "{}"))).hasMessage("payment_not_found [OTHER-1]");
    }

    @Test
    void anApprovalRefusedByTheDatabaseIsA409() {
        Payment payment = pendingPayment();
        when(payments.saveAndFlush(payment)).thenThrow(new JpaSystemException(new RuntimeException(
                new SQLException("La reserva expiró o no existe al aprobar el pago", "P0001"))));

        assertThatThrownBy(() -> webhooks.process(event("evt-4", PaymentStatus.APPROVED)))
                .hasMessage("payment_approval_rejected [PAY-7]");
    }

    private Payment pendingPayment() {
        Payment payment = new Payment();
        payment.setPaymentId(7L);
        payment.setObligation(obligation);
        payment.setGateway("wompi");
        payment.setStatus(PaymentStatus.PENDING);
        payment.setAmount(obligation.getAmount());
        payment.setCurrency("COP");
        when(payments.findById(7L)).thenReturn(Optional.of(payment));
        return payment;
    }

    private static GatewayEvent event(String id, PaymentStatus status) {
        return new GatewayEvent("wompi", id, "PAY-7", "tx-1", status, "{}");
    }
}
