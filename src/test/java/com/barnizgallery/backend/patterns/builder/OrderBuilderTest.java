package com.barnizgallery.backend.patterns.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.OrderItem;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.FulfillmentStatus;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.enums.OrderStatus;
import com.barnizgallery.backend.enums.PaymentObligationStage;

class OrderBuilderTest {

    @Test
    void amountsFollowTheDatabaseRules() {
        Artwork tray = artwork(1, "33.35", "USD");
        Artwork box = artwork(2, "120", "USD");

        BuiltOrder built = builder().artwork(tray, 3).artwork(box, 1).build();

        OrderItem first = built.items().get(0);
        assertThat(first.getLineTotal()).isEqualByComparingTo("100.05");
        assertThat(first.getPlatformFeeAmount()).isEqualByComparingTo("10.01");
        assertThat(first.getArtisanNetAmount()).isEqualByComparingTo("90.04");
        assertThat(first.getArtisanGrossAmount()).isEqualByComparingTo(
                first.getPlatformFeeAmount().add(first.getArtisanNetAmount()));
        assertThat(first.getFulfillmentStatus()).isEqualTo(FulfillmentStatus.PENDING);
        assertThat(built.order().getSubtotal()).isEqualByComparingTo("220.05");
        assertThat(built.order().getPlatformFee()).isEqualByComparingTo("22.01");
        assertThat(built.order().getTotal()).isEqualByComparingTo("245.05");
        assertThat(built.order().getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(built.obligations()).singleElement().satisfies(o -> {
            assertThat(o.getStage()).isEqualTo(PaymentObligationStage.SINGLE_PAYMENT);
            assertThat(o.getAmount()).isEqualByComparingTo("245.05");
        });
    }

    @Test
    void linesMustShareTheOrderCurrencyAndARateBelowOne() {
        assertThatThrownBy(() -> builder().artwork(artwork(1, "10", "COP"), 1).build())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> builder().commissionRate(BigDecimal.ONE).artwork(artwork(1, "10", "USD"), 1).build())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> builder().build()).isInstanceOf(IllegalStateException.class);
    }

    private static OrderBuilder builder() {
        return new OrderBuilder().code("MUS-TEST").buyer(TestEntities.user(4, Language.ES)).currency("USD")
                .commissionRate(new BigDecimal("0.10")).shipping(new BigDecimal("25")).shippingAddress("{}");
    }

    private static Artwork artwork(long id, String price, String currency) {
        Artwork artwork = TestEntities.artwork(id, TestEntities.store(1, TestEntities.master(1)),
                ArtworkStatus.EXHIBITED);
        artwork.setPrice(new BigDecimal(price));
        artwork.setCurrency(currency);
        return artwork;
    }
}
