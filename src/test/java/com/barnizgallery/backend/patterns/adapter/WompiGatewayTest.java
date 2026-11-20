package com.barnizgallery.backend.patterns.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.barnizgallery.backend.config.WompiProperties;
import com.barnizgallery.backend.enums.PaymentStatus;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.exception.FeatureDisabledException;

class WompiGatewayTest {

    private final String integritySecret = "test_integrity_" + UUID.randomUUID();
    private final String eventsSecret = "test_events_" + UUID.randomUUID();
    private final WompiGateway gateway = new WompiGateway(new WompiProperties("pub_test_" + UUID.randomUUID(),
            integritySecret, eventsSecret, "https://checkout.wompi.co/p/"));

    /** The example of the Wompi documentation (Widget &amp; Checkout Web, integrity signature). */
    @Test
    void integritySignatureMatchesTheDocumentedExample() {
        assertThat(WompiSignatures.integrity("sk8-438k4-xmxm392-sn2m", 2490000, "COP", null,
                "prod_integrity_Z5mMke9x0k8gpErbDqwrJXMqsI6SFli6"))
                .isEqualTo("37c8407747e595535433ef8f6a811d853cd943046624a0ec04662b17bbf33bf5");
    }

    @Test
    void amountsGoInCents() {
        assertThat(WompiSignatures.amountInCents(new BigDecimal("24900.00"))).isEqualTo(2490000);
        assertThat(WompiSignatures.amountInCents(new BigDecimal("24900.5"))).isEqualTo(2490050);
        assertThatThrownBy(() -> WompiSignatures.amountInCents(new BigDecimal("1.234")))
                .isInstanceOf(ArithmeticException.class);
    }

    @Test
    void checkoutUrlCarriesTheSignedPayment() {
        Instant expiresAt = Instant.parse("2026-11-20T15:30:00Z");
        CheckoutSession session = gateway.startCheckout(new CheckoutRequest("PAY-7", new BigDecimal("24900"), "COP",
                "ana@mail.com", "Pedido 7", "https://museo.test/orders/7", null, expiresAt));

        String signature = WompiSignatures.integrity("PAY-7", 2490000, "COP", "2026-11-20T15:30:00.000Z",
                integritySecret);
        assertThat(session.gateway()).isEqualTo("wompi");
        assertThat(session.gatewayReference()).isEqualTo("PAY-7");
        assertThat(session.redirectUrl()).startsWith("https://checkout.wompi.co/p/?public-key=pub_test_")
                .contains("currency=COP", "amount-in-cents=2490000", "reference=PAY-7",
                        "signature:integrity=" + signature, "expiration-time=2026-11-20T15:30:00.000Z",
                        "redirect-url=https://museo.test/orders/7", "customer-data:email=ana@mail.com")
                .doesNotContain(integritySecret);
    }

    @Test
    void onlyCopIsCharged() {
        assertThatThrownBy(() -> gateway.startCheckout(new CheckoutRequest("PAY-8", BigDecimal.TEN, "USD", null,
                null, null, null, null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("payment_currency_not_supported [USD]");
    }

    @Test
    void withoutCredentialsTheGatewayIsDisabled() {
        WompiGateway disabled = new WompiGateway(new WompiProperties("", "", "", "https://checkout.wompi.co/p/"));

        assertThat(disabled.isEnabled()).isFalse();
        assertThatThrownBy(() -> disabled.startCheckout(new CheckoutRequest("PAY-9", BigDecimal.TEN, "COP", null,
                null, null, null, null)))
                .isInstanceOf(FeatureDisabledException.class);
    }

    @Test
    void validEventChangesThePayment() {
        String checksum = WompiSignatures.sha256Hex("1234-1610641025-49201APPROVED44900001530291411" + eventsSecret);

        GatewayEvent event = gateway.readEvent(call(event("transaction.updated", "APPROVED", checksum))).orElseThrow();

        assertThat(event.gateway()).isEqualTo("wompi");
        assertThat(event.eventId()).isEqualTo("1234-1610641025-49201:APPROVED");
        assertThat(event.reference()).isEqualTo("MZQ3X2DE2SMX");
        assertThat(event.gatewayTransactionId()).isEqualTo("1234-1610641025-49201");
        assertThat(event.status()).isEqualTo(PaymentStatus.APPROVED);
    }

    @Test
    void checksumIsCaseInsensitiveAndTheHeaderWins() {
        String checksum = WompiSignatures.sha256Hex("1234-1610641025-49201DECLINED44900001530291411" + eventsSecret);
        String body = event("transaction.updated", "DECLINED", "not-the-checksum");

        GatewayEvent event = gateway.readEvent(new WebhookCall(Map.of("X-Event-Checksum",
                checksum.toUpperCase()), body)).orElseThrow();

        assertThat(event.status()).isEqualTo(PaymentStatus.REJECTED);
    }

    @Test
    void tamperedEventsAreRejected() {
        String checksum = WompiSignatures.sha256Hex("1234-1610641025-49201DECLINED44900001530291411" + eventsSecret);

        assertThatThrownBy(() -> gateway.readEvent(call(event("transaction.updated", "APPROVED", checksum))))
                .hasMessage("invalid_webhook_signature");
        assertThatThrownBy(() -> gateway.readEvent(call("{not json"))).hasMessage("invalid_webhook_signature");
    }

    @Test
    void otherEventsAreIgnored() {
        String checksum = WompiSignatures.sha256Hex("1234-1610641025-49201APPROVED44900001530291411" + eventsSecret);

        assertThat(gateway.readEvent(call(event("nequi_token.updated", "APPROVED", checksum)))).isEmpty();
    }

    @Test
    void wompiStatusesMapToPaymentStatuses() {
        assertThat(WompiGateway.toPaymentStatus("VOIDED")).isEqualTo(PaymentStatus.REJECTED);
        assertThat(WompiGateway.toPaymentStatus("ERROR")).isEqualTo(PaymentStatus.REJECTED);
        assertThat(WompiGateway.toPaymentStatus("PENDING")).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void simulatedGatewayTakesAnyCurrencyAndHasNoWebhooks() {
        SimulatedGateway simulated = new SimulatedGateway("https://museo.test/");
        CheckoutSession session = simulated.startCheckout(new CheckoutRequest("PAY 10", BigDecimal.ONE, "EUR", null,
                null, null, null, null));

        assertThat(simulated.currencies()).isEmpty();
        assertThat(session.redirectUrl()).isEqualTo("https://museo.test/payments/simulated?reference=PAY+10");
        assertThat(session.gatewayReference()).isEqualTo("sim_PAY 10");
        assertThatThrownBy(() -> simulated.readEvent(call("{}"))).hasMessage("webhook_not_supported [simulated]");
    }

    /** Same fields and values as the event example of the Wompi documentation, with our own checksum. */
    private static String event(String type, String status, String checksum) {
        return """
                {"event": "%s",
                 "data": {"transaction": {"id": "1234-1610641025-49201", "amount_in_cents": 4490000,
                   "reference": "MZQ3X2DE2SMX", "currency": "COP", "status": "%s"}},
                 "environment": "test",
                 "signature": {"properties": ["transaction.id", "transaction.status", "transaction.amount_in_cents"],
                   "checksum": "%s"},
                 "timestamp": 1530291411, "sent_at": "2018-07-20T16:45:05.000Z"}""".formatted(type, status, checksum);
    }

    private static WebhookCall call(String body) {
        return new WebhookCall(Map.of(), body);
    }
}
