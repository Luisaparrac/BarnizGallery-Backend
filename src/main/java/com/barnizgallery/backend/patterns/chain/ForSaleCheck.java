package com.barnizgallery.backend.patterns.chain;

import java.util.List;
import java.util.function.Predicate;

import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.CartItem;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.BusinessRuleException;

/**
 * <b>Chain of Responsibility – Concrete handler:</b> every artwork of the cart can still be bought.
 */
public class ForSaleCheck extends CheckoutCheck {

    private final Predicate<Artwork> purchasable;

    public ForSaleCheck(Predicate<Artwork> purchasable) {
        this.purchasable = purchasable;
    }

    @Override
    protected void check(List<CartItem> lines) {
        for (CartItem line : lines) {
            if (!purchasable.test(line.getArtwork())) {
                throw new BusinessRuleException(ErrorCode.ARTWORK_NOT_FOR_SALE, line.getArtwork().getArtworkId());
            }
        }
    }
}
