package com.barnizgallery.backend.patterns.chain;

import java.util.List;

import com.barnizgallery.backend.entity.CartItem;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.BusinessRuleException;

/**
 * <b>Chain of Responsibility – Concrete handler:</b> the cart has at least one line.
 */
public class CartNotEmptyCheck extends CheckoutCheck {

    @Override
    protected void check(List<CartItem> lines) {
        if (lines.isEmpty()) {
            throw new BusinessRuleException(ErrorCode.CART_EMPTY);
        }
    }
}
