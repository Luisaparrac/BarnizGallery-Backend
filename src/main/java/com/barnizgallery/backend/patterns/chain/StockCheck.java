package com.barnizgallery.backend.patterns.chain;

import java.util.List;

import com.barnizgallery.backend.entity.CartItem;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.BusinessRuleException;

/**
 * <b>Chain of Responsibility – Concrete handler:</b> no line asks for more units than the artwork has now. The
 * database still has the last word when it reserves the stock.
 */
public class StockCheck extends CheckoutCheck {

    @Override
    protected void check(List<CartItem> lines) {
        for (CartItem line : lines) {
            if (line.getQuantity() > line.getArtwork().getStock()) {
                throw new BusinessRuleException(ErrorCode.NOT_ENOUGH_STOCK, line.getArtwork().getStock());
            }
        }
    }
}
