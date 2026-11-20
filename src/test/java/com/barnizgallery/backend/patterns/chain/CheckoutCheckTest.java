package com.barnizgallery.backend.patterns.chain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.CartItem;
import com.barnizgallery.backend.enums.ArtworkStatus;

class CheckoutCheckTest {

    private final CheckoutCheck chain = chain();

    @Test
    void theFirstBrokenRuleStopsTheChain() {
        assertThatThrownBy(() -> chain.handle(List.of())).hasMessage("cart_empty");
        assertThatThrownBy(() -> chain.handle(List.of(line(artwork(1, "USD", 0), 1))))
                .hasMessage("artwork_not_for_sale [1]");
        assertThatThrownBy(() -> chain.handle(List.of(line(artwork(2, "USD", 1), 2))))
                .hasMessage("not_enough_stock [1]");
        assertThatThrownBy(() -> chain.handle(List.of(line(artwork(2, "USD", 1), 1), line(artwork(3, "COP", 1), 1))))
                .hasMessage("cart_mixed_currencies [USD, COP]");
    }

    @Test
    void aValidCartPassesEveryHandler() {
        assertThatCode(() -> chain.handle(List.of(line(artwork(2, "USD", 3), 2), line(artwork(4, "USD", 1), 1))))
                .doesNotThrowAnyException();
    }

    private static CheckoutCheck chain() {
        CheckoutCheck first = new CartNotEmptyCheck();
        first.then(new ForSaleCheck(artwork -> artwork.getArtworkId() != 1L))
                .then(new StockCheck())
                .then(new SingleCurrencyCheck());
        return first;
    }

    private static Artwork artwork(long id, String currency, int stock) {
        Artwork artwork = TestEntities.artwork(id, TestEntities.store(1, TestEntities.master(1)), ArtworkStatus.EXHIBITED);
        artwork.setPrice(BigDecimal.TEN);
        artwork.setCurrency(currency);
        artwork.setStock(stock);
        return artwork;
    }

    private static CartItem line(Artwork artwork, int quantity) {
        CartItem item = new CartItem();
        item.setArtwork(artwork);
        item.setQuantity(quantity);
        return item;
    }
}
