package com.barnizgallery.backend.service;

import java.math.BigDecimal;

import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.enums.SaleMode;
import com.barnizgallery.backend.exception.BusinessRuleException;

/**
 * Price, stock and sale mode rules of an artwork (422 when broken). A price always needs its currency; an artwork
 * that is not a draft needs a sale mode, and one for sale needs a price above zero and a stock.
 */
final class ArtworkSaleRules {

    private ArtworkSaleRules() {
    }

    static void validate(Artwork artwork) {
        validate(artwork, artwork.getStatus());
    }

    /** Checks the artwork as if it had the given status, before the status is changed. */
    static void validate(Artwork artwork, ArtworkStatus status) {
        if (artwork.getPrice() != null && artwork.getCurrency() == null) {
            throw BusinessRuleException.unprocessable(ErrorCode.ARTWORK_CURRENCY_REQUIRED);
        }
        if (status == ArtworkStatus.DRAFT || status == ArtworkStatus.ARCHIVED) {
            return;
        }
        if (artwork.getSaleMode() == null) {
            throw BusinessRuleException.unprocessable(ErrorCode.ARTWORK_SALE_MODE_REQUIRED);
        }
        if (artwork.getSaleMode() == SaleMode.SALE) {
            if (artwork.getPrice() == null || artwork.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw BusinessRuleException.unprocessable(ErrorCode.ARTWORK_PRICE_REQUIRED);
            }
            if (artwork.getStock() == null) {
                throw BusinessRuleException.unprocessable(ErrorCode.ARTWORK_STOCK_REQUIRED);
            }
        }
    }
}
