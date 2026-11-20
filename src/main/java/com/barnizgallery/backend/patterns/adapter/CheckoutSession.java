package com.barnizgallery.backend.patterns.adapter;

/**
 * A payment prepared in a gateway.
 *
 * @param gateway          code of the gateway ({@code payments.gateway})
 * @param gatewayReference id of the payment in the gateway, for example the PayPal order id
 *                         ({@code payments.gateway_reference})
 * @param redirectUrl      page where the buyer pays
 */
public record CheckoutSession(String gateway, String gatewayReference, String redirectUrl) {
}
