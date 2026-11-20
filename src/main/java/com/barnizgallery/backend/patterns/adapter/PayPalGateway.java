package com.barnizgallery.backend.patterns.adapter;

import java.math.RoundingMode;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barnizgallery.backend.config.PayPalProperties;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.enums.PaymentStatus;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.exception.FeatureDisabledException;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * <b>Adapter pattern – Adapter</b> for PayPal (REST API, Orders v2, without the SDK).
 * <p>
 * PayPal charges the USD obligations:
 * <ol>
 * <li>a checkout creates an order with {@code intent=CAPTURE}; the payment reference travels as
 * {@code reference_id} and {@code custom_id}, and the buyer pays in the {@code payer-action} (or {@code approve})
 * link;</li>
 * <li>when the buyer approves, PayPal sends {@code CHECKOUT.ORDER.APPROVED} and the order is captured here;</li>
 * <li>{@code PAYMENT.CAPTURE.*} events then report the final result.</li>
 * </ol>
 * Every webhook is verified by PayPal before it is used.
 */
public class PayPalGateway implements PaymentGateway {

    public static final String CODE = "paypal";
    public static final String CURRENCY = "USD";

    static final String BRAND_NAME = "MUSEO Barniz de Pasto";
    static final int MAX_DESCRIPTION = 127;

    private static final Logger log = LoggerFactory.getLogger(PayPalGateway.class);
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final PayPalApiClient api;
    private final PayPalWebhookVerifier verifier;
    private final PayPalProperties properties;

    public PayPalGateway(PayPalApiClient api, PayPalWebhookVerifier verifier, PayPalProperties properties) {
        this.api = api;
        this.verifier = verifier;
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
        requireEnabled();
        JsonNode order = api.post("/v2/checkout/orders", "create-" + request.reference(), orderBody(request));
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

    /**
     * Verifies the webhook with PayPal, then reads it. An approved order is captured (the capture uses the
     * order id as {@code PayPal-Request-Id}, so a repeated event does not capture twice). Other event types
     * are ignored.
     */
    @Override
    public Optional<GatewayEvent> readEvent(WebhookCall call) {
        requireEnabled();
        JsonNode event = parse(call.body());
        if (!verifier.isValid(call, event)) {
            throw BusinessRuleException.badRequest(ErrorCode.INVALID_WEBHOOK_SIGNATURE);
        }
        String eventId = event.path("id").asString();
        String type = event.path("event_type").asString();
        JsonNode resource = event.path("resource");
        if ("CHECKOUT.ORDER.APPROVED".equals(type)) {
            String orderId = resource.path("id").asString();
            String reference = resource.path("purchase_units").path(0).path("custom_id").asString();
            JsonNode capture = api.post("/v2/checkout/orders/" + orderId + "/capture", "capture-" + orderId,
                    JSON.createObjectNode()).path("purchase_units").path(0).path("payments").path("captures").path(0);
            return Optional.of(new GatewayEvent(CODE, eventId, reference, capture.path("id").asString(),
                    captureStatus(capture.path("status").asString()), call.body()));
        }
        if (type.startsWith("PAYMENT.CAPTURE.")) {
            return Optional.of(new GatewayEvent(CODE, eventId, resource.path("custom_id").asString(),
                    resource.path("id").asString(), captureStatus(resource.path("status").asString()), call.body()));
        }
        return Optional.empty();
    }

    /** COMPLETED → approved; DECLINED and FAILED → declined; REFUNDED → refunded; anything else → pending. */
    static PaymentStatus captureStatus(String paypalStatus) {
        return switch (paypalStatus) {
            case "COMPLETED" -> PaymentStatus.APPROVED;
            case "DECLINED", "FAILED" -> PaymentStatus.DECLINED;
            case "REFUNDED", "PARTIALLY_REFUNDED" -> PaymentStatus.REFUNDED;
            default -> PaymentStatus.PENDING;
        };
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

    private void requireEnabled() {
        if (!isEnabled()) {
            throw new FeatureDisabledException(ErrorCode.PAYMENT_GATEWAY_DISABLED, CURRENCY);
        }
    }

    private static JsonNode parse(String body) {
        try {
            return JSON.readTree(body);
        } catch (JacksonException ex) {
            throw BusinessRuleException.badRequest(ErrorCode.INVALID_WEBHOOK_SIGNATURE);
        }
    }
}
