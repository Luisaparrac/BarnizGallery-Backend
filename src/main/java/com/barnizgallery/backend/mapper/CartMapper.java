package com.barnizgallery.backend.mapper;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.barnizgallery.backend.dto.CartItemResponse;
import com.barnizgallery.backend.dto.CartResponse;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.CartItem;

/**
 * Builds the cart DTO; the availability of each line is decided by the service.
 */
public final class CartMapper {

    private CartMapper() {
    }

    public static CartItemResponse toResponse(CartItem item, boolean available, String coverUrl) {
        Artwork artwork = item.getArtwork();
        BigDecimal price = artwork.getPrice();
        return new CartItemResponse(artwork.getArtworkId(), artwork.getStore().getStoreId(), artwork.getTitleEs(),
                artwork.getTitleEs(),
                artwork.getTitleEn(), price, artwork.getCurrency(), item.getQuantity(),
                price == null ? null : price.multiply(BigDecimal.valueOf(item.getQuantity())), artwork.getStock(),
                available, coverUrl);
    }

    /** The cart with the subtotal of its available lines per currency. */
    public static CartResponse toResponse(List<CartItemResponse> items) {
        Map<String, BigDecimal> subtotals = new LinkedHashMap<>();
        items.stream().filter(CartItemResponse::available)
                .forEach(item -> subtotals.merge(item.currency(), item.lineTotal(), BigDecimal::add));
        boolean single = subtotals.size() == 1;
        return new CartResponse(items, subtotals, single ? subtotals.values().iterator().next() : null,
                single ? subtotals.keySet().iterator().next() : null);
    }
}
