package com.barnizgallery.backend.patterns.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.barnizgallery.backend.config.PayPalProperties;
import com.barnizgallery.backend.enums.PaymentStatus;
import com.barnizgallery.backend.exception.FeatureDisabledException;

/** PayPal is simulated with MockRestServiceServer; no real call is made. */
class PayPalGatewayTest {

    private static final String BASE_URL = "https://paypal.test";
    private static final Map<String, String> TRANSMISSION = Map.of(
            "PAYPAL-AUTH-ALGO", "SHA256withRSA",
            "PAYPAL-CERT-URL", "https://api-m.sandbox.paypal.com/v1/notifications/certs/CERT-1",
            "PAYPAL-TRANSMISSION-ID", "69cd13f0-d67a-11e5-baa3-778b53f4ae55",
            "PAYPAL-TRANSMISSION-SIG", "lmI95Jx3Y9nhR5SJWlHVIWpg4AgFk7n9bCHSRxbrd8A9zrhdu2rMyFrmz+Zjh3s3boXB07VXCXUZy",
            "PAYPAL-TRANSMISSION-TIME", "2026-11-19T12:00:00Z");

    private final String clientId = UUID.randomUUID().toString();
    private final String clientSecret = UUID.randomUUID().toString();
    private final String webhookId = "WH-" + UUID.randomUUID();
    private final PayPalProperties properties = new PayPalProperties(clientId, clientSecret, webhookId, BASE_URL,
            Duration.ofSeconds(15));
    private MockRestServiceServer server;
    private PayPalGateway gateway;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        PayPalApiClient api = new PayPalApiClient(restClient,
                new PayPalTokenClient(restClient, properties, Clock.systemUTC()));
        gateway = new PayPalGateway(api, new PayPalWebhookVerifier(api, properties), properties);
    }

    @Test
    void createsACaptureOrderAndReusesTheOauthToken() {
        String basic = Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));
        server.expect(once(), requestTo(BASE_URL + "/v1/oauth2/token"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Basic " + basic))
                .andExpect(content().formDataContains(Map.of("grant_type", "client_credentials")))
                .andRespond(withSuccess("""
                        {"access_token": "A21AA-test", "token_type": "Bearer", "expires_in": 32400}""",
                        MediaType.APPLICATION_JSON));
        for (String reference : new String[] { "PAY-1", "PAY-2" }) {
            server.expect(requestTo(BASE_URL + "/v2/checkout/orders"))
                    .andExpect(header("Authorization", "Bearer A21AA-test"))
                    .andExpect(header("PayPal-Request-Id", "create-" + reference))
                    .andExpect(jsonPath("$.intent").value("CAPTURE"))
                    .andExpect(jsonPath("$.purchase_units[0].custom_id").value(reference))
                    .andExpect(jsonPath("$.purchase_units[0].amount.currency_code").value("USD"))
                    .andExpect(jsonPath("$.purchase_units[0].amount.value").value("49.90"))
                    .andExpect(jsonPath("$.payment_source.paypal.experience_context.return_url")
                            .value("https://museo.test/orders/1"))
                    .andRespond(withSuccess("""
                            {"id": "5O190127TN364715T", "status": "PAYER_ACTION_REQUIRED",
                             "links": [{"href": "https://api-m.sandbox.paypal.com/v2/checkout/orders/5O190127TN364715T",
                                        "rel": "self", "method": "GET"},
                                       {"href": "https://www.sandbox.paypal.com/checkoutnow?token=5O190127TN364715T",
                                        "rel": "payer-action", "method": "GET"}]}""", MediaType.APPLICATION_JSON));
        }

        CheckoutSession first = gateway.startCheckout(request("PAY-1", "USD"));
        gateway.startCheckout(request("PAY-2", "USD"));

        assertThat(first).isEqualTo(new CheckoutSession("paypal", "5O190127TN364715T",
                "https://www.sandbox.paypal.com/checkoutnow?token=5O190127TN364715T"));
        server.verify();
    }

    @Test
    void onlyUsdIsCharged() {
        assertThatThrownBy(() -> gateway.startCheckout(request("PAY-3", "COP")))
                .hasMessage("payment_currency_not_supported [COP]");
    }

    @Test
    void paypalErrorsBecomeGatewayUnavailable() {
        server.expect(requestTo(BASE_URL + "/v1/oauth2/token"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\": \"invalid_client\"}"));

        assertThatThrownBy(() -> gateway.startCheckout(request("PAY-4", "USD")))
                .isInstanceOf(FeatureDisabledException.class)
                .hasMessage("payment_gateway_unavailable");
    }

    @Test
    void approvedOrderIsVerifiedAndCaptured() {
        expectToken();
        server.expect(requestTo(BASE_URL + "/v1/notifications/verify-webhook-signature"))
                .andExpect(jsonPath("$.auth_algo").value("SHA256withRSA"))
                .andExpect(jsonPath("$.transmission_id").value("69cd13f0-d67a-11e5-baa3-778b53f4ae55"))
                .andExpect(jsonPath("$.webhook_id").value(webhookId))
                .andExpect(jsonPath("$.webhook_event.id").value("WH-EVENT-1"))
                .andRespond(withSuccess("{\"verification_status\": \"SUCCESS\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE_URL + "/v2/checkout/orders/5O190127TN364715T/capture"))
                .andExpect(header("PayPal-Request-Id", "capture-5O190127TN364715T"))
                .andRespond(withSuccess("""
                        {"id": "5O190127TN364715T", "status": "COMPLETED",
                         "purchase_units": [{"reference_id": "PAY-1", "payments": {"captures": [
                           {"id": "3C679366HH908993F", "status": "COMPLETED"}]}}]}""", MediaType.APPLICATION_JSON));

        GatewayEvent event = gateway.readEvent(new WebhookCall(TRANSMISSION, """
                {"id": "WH-EVENT-1", "event_type": "CHECKOUT.ORDER.APPROVED",
                 "resource": {"id": "5O190127TN364715T", "status": "APPROVED",
                   "purchase_units": [{"reference_id": "PAY-1", "custom_id": "PAY-1"}]}}""")).orElseThrow();

        assertThat(event).isEqualTo(new GatewayEvent("paypal", "WH-EVENT-1", "PAY-1", "3C679366HH908993F",
                PaymentStatus.APPROVED, event.payload()));
        server.verify();
    }

    @Test
    void captureEventsReportTheResult() {
        expectToken();
        server.expect(requestTo(BASE_URL + "/v1/notifications/verify-webhook-signature"))
                .andRespond(withSuccess("{\"verification_status\": \"SUCCESS\"}", MediaType.APPLICATION_JSON));

        GatewayEvent event = gateway.readEvent(new WebhookCall(TRANSMISSION, """
                {"id": "WH-EVENT-2", "event_type": "PAYMENT.CAPTURE.DECLINED",
                 "resource": {"id": "3C679366HH908993F", "status": "DECLINED", "custom_id": "PAY-1"}}""")).orElseThrow();

        assertThat(event.status()).isEqualTo(PaymentStatus.DECLINED);
        assertThat(event.reference()).isEqualTo("PAY-1");
    }

    @Test
    void unverifiedWebhooksAreRejected() {
        expectToken();
        server.expect(requestTo(BASE_URL + "/v1/notifications/verify-webhook-signature"))
                .andRespond(withSuccess("{\"verification_status\": \"FAILURE\"}", MediaType.APPLICATION_JSON));
        String body = "{\"id\": \"WH-EVENT-3\", \"event_type\": \"PAYMENT.CAPTURE.COMPLETED\", \"resource\": {}}";

        assertThatThrownBy(() -> gateway.readEvent(new WebhookCall(TRANSMISSION, body)))
                .hasMessage("invalid_webhook_signature");
        assertThatThrownBy(() -> gateway.readEvent(new WebhookCall(Map.of(), body)))
                .hasMessage("invalid_webhook_signature");
        server.verify();
    }

    @Test
    void captureStatusesMapToPaymentStatuses() {
        assertThat(PayPalGateway.captureStatus("COMPLETED")).isEqualTo(PaymentStatus.APPROVED);
        assertThat(PayPalGateway.captureStatus("FAILED")).isEqualTo(PaymentStatus.DECLINED);
        assertThat(PayPalGateway.captureStatus("REFUNDED")).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(PayPalGateway.captureStatus("PENDING")).isEqualTo(PaymentStatus.PENDING);
    }

    private void expectToken() {
        server.expect(once(), requestTo(BASE_URL + "/v1/oauth2/token"))
                .andRespond(withSuccess("{\"access_token\": \"A21AA-test\", \"token_type\": \"Bearer\", "
                        + "\"expires_in\": 32400}", MediaType.APPLICATION_JSON));
    }

    private static CheckoutRequest request(String reference, String currency) {
        return new CheckoutRequest(reference, new BigDecimal("49.9"), currency, "ana@mail.com", "Pedido 1",
                "https://museo.test/orders/1", "https://museo.test/cart", null);
    }
}
