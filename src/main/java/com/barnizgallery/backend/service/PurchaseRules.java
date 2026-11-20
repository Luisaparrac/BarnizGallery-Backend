package com.barnizgallery.backend.service;

import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Store;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.enums.MasterVerificationStatus;
import com.barnizgallery.backend.enums.SaleMode;
import com.barnizgallery.backend.enums.StoreStatus;
import com.barnizgallery.backend.exception.BusinessRuleException;

/**
 * When an artwork can be bought directly: exhibited for sale, not deleted, with price, currency and stock, in a
 * published store of an active, approved master. Stock itself is checked and reserved by the database at checkout.
 */
final class PurchaseRules {

    private PurchaseRules() {
    }

    static boolean isPurchasable(Artwork artwork) {
        Store store = artwork.getStore();
        return artwork.getDeletedAt() == null
                && artwork.getStatus() == ArtworkStatus.EXHIBITED
                && artwork.getSaleMode() == SaleMode.SALE
                && artwork.getPrice() != null && artwork.getCurrency() != null && artwork.getStock() != null
                && store.getStatus() == StoreStatus.PUBLISHED
                && store.getMaster().isActive()
                && store.getMaster().getVerificationStatus() == MasterVerificationStatus.APPROVED;
    }

    /** 409 {@code artwork_not_for_sale} unless the artwork can be bought. */
    static void requirePurchasable(Artwork artwork) {
        if (!isPurchasable(artwork)) {
            throw new BusinessRuleException(ErrorCode.ARTWORK_NOT_FOR_SALE, artwork.getArtworkId());
        }
    }
}
