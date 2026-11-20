package com.barnizgallery.backend.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * The cart of the logged-in user and the subtotal of its available lines per currency. Adding to the cart does not
 * reserve anything: stock is reserved at checkout.
 */
public record CartResponse(List<CartItemResponse> items, Map<String, BigDecimal> subtotals) {
}
