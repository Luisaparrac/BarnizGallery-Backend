package com.barnizgallery.backend.dto;

/**
 * Result of accepting an offer: the request and the {@code encargo} order created with its deposit and balance.
 */
public record AcceptOfferResponse(CustomRequestResponse request, Long orderId) {
}
