package com.barnizgallery.backend.patterns.adapter;

import java.math.RoundingMode;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.barnizgallery.backend.config.PayPalProperties;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.exception.FeatureDisabledException;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * <b>Adapter pattern – Adapter</b> for PayPal (REST API, Orders v2, without the SDK).
 * <p>
 * PayPal charges the USD obligations. A checkout creates an order with {@code intent=CAPTURE}; the payment
 * reference travels as {@code reference_id} and {@code custom_id}, and the buyer is sent to the
 * {@code payer-action} (or {@code approve}) link to pay.
 */
public class PayPalGateway implements PaymentGateway {

    public static final String CODE = "paypal";
    public static final String CURRENCY = "USD";

    static final String REQUEST_ID_HEADER = "PayPal-Request-Id";
    static final String BRAND_NAME = "MUSEO Barniz de Pasto";
    static final int MAX_DESCRIPTION = 127;

    private static final Logger log = LoggerFactory.getLogger(PayPalGateway.class);
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final RestClient restClient;
    private final PayPalTokenClient tokenClient;
    private final PayPalProperties properties;

    public PayPalGateway(RestClient restClient, PayPalTokenClient tokenClient, PayPalProperties properties) {
        this.restClient = restClient;
        this.tokenClient = tokenClient;
        this.properties = properties;
    }

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public Set<String> currencies() {
        return Set.of(CURRENCY);
    }

    @Override
    public boolean isEnabled() {
        return properties.isConfigured();
    }

    @Override
    public CheckoutSession startCheckout(CheckoutRequest request) {
        if (!CURRENCY.equals(request.currency())) {
            throw new BusinessRuleException(ErrorCode.PAYMENT_CURRENCY_NOT_SUPPORTED, request.currency());
        }
        if (!isEnabled()) {
            throw new FeatureDisabledException(ErrorCode.PAYMENT_GATEWAY_DISABLED, CURRENCY);
        }
        JsonNode order = post("/v2/checkout/orders", "create-" + request.reference(), orderBody(request));
        String orderId = order.path("id").asString();
        String payUrl = null;
        for (JsonNode link : order.path("links").values()) {
            String rel = link.path("rel").asString();
            if ("payer-action".equals(rel) || "approve".equals(rel)) {
                payUrl = link.path("href").asString();
            }
        }
        if (orderId.isBlank() || payUrl == null) {
            log.warn("PayPal created an order without id or payment link");
            throw new FeatureDisabledException(ErrorCode.PAYMENT_GATEWAY_UNAVAILABLE);
        }
        return new CheckoutSession(CODE, orderId, payUrl);
    }

    @Override
    public Optional<GatewayEvent> readEvent(WebhookCall call) {
        throw BusinessRuleException.badRequest(ErrorCode.WEBHOOK_NOT_SUPPORTED, CODE);
    }

    static ObjectNode orderBody(CheckoutRequest request) {
        ObjectNode body = JSON.createObjectNode();
        body.put("intent", "CAPTURE");
        ObjectNode unit = body.putArray("purchase_units").addObject();
        unit.put("reference_id", request.reference());
        unit.put("custom_id", request.reference());
        if (request.description() != null) {
            String description = request.description();
            unit.put("description", description.length() > MAX_DESCRIPTION
                    ? description.substring(0, MAX_DESCRIPTION) : description);
        }
        ObjectNode amount = unit.putObject("amount");
        amount.put("currency_code", CURRENCY);
        amount.put("value", request.amount().setScale(2, RoundingMode.UNNECESSARY).toPlainString());
        ObjectNode context = body.putObject("payment_source").putObject("paypal").putObject("experience_context");
        context.put("brand_name", BRAND_NAME);
        context.put("shipping_preference", "NO_SHIPPING");
        context.put("user_action", "PAY_NOW");
        if (request.returnUrl() != null) {
            context.put("return_url", request.returnUrl());
        }
        if (request.cancelUrl() != null) {
            context.put("cancel_url", request.cancelUrl());
        }
        return body;
    }

    /** Authenticated JSON POST; the request id makes PayPal ignore a repeated call. */
    JsonNode post(String path, String requestId, Object body) {
        try {
            JsonNode answer = restClient.post().uri(path)
                    .headers(headers -> headers.setBearerAuth(tokenClient.accessToken()))
                    .header(REQUEST_ID_HEADER, requestId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
            if (answer == null) {
                throw new FeatureDisabledException(ErrorCode.PAYMENT_GATEWAY_UNAVAILABLE);
            }
            return answer;
        } catch (RestClientResponseException ex) {
            log.warn("PayPal answered HTTP {} on {}", ex.getStatusCode().value(), path);
            throw new FeatureDisabledException(ErrorCode.PAYMENT_GATEWAY_UNAVAILABLE);
        } catch (RestClientException ex) {
            log.warn("PayPal did not answer on {}: {}", path, ex.getClass().getSimpleName());
            throw new FeatureDisabledException(ErrorCode.PAYMENT_GATEWAY_UNAVAILABLE);
        }
    }
}
