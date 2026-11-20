package com.barnizgallery.backend.patterns.chain;

import java.util.List;

import com.barnizgallery.backend.entity.CartItem;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.BusinessRuleException;

/**
 * <b>Chain of Responsibility – Concrete handler:</b> all lines share one currency, because an order has one.
 */
public class SingleCurrencyCheck extends CheckoutCheck {

    @Override
    protected void check(List<CartItem> lines) {
        List<String> currencies = lines.stream().map(line -> line.getArtwork().getCurrency()).distinct().toList();
        if (currencies.size() > 1) {
            throw new BusinessRuleException(ErrorCode.CART_MIXED_CURRENCIES, String.join(", ", currencies));
        }
    }
}
