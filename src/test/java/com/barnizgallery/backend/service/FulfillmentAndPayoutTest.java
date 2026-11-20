package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.dto.PayoutResponse;
import com.barnizgallery.backend.dto.ShipmentRequest;
import com.barnizgallery.backend.entity.Order;
import com.barnizgallery.backend.entity.OrderItem;
import com.barnizgallery.backend.entity.Payout;
import com.barnizgallery.backend.entity.PayoutItem;
import com.barnizgallery.backend.entity.Store;
import com.barnizgallery.backend.enums.FulfillmentStatus;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.enums.OrderStatus;
import com.barnizgallery.backend.enums.PayoutStatus;
import com.barnizgallery.backend.repository.OrderItemRepository;
import com.barnizgallery.backend.repository.PayoutItemRepository;
import com.barnizgallery.backend.repository.PayoutRepository;

/** Shipping of paid lines by the artisan and payouts of delivered lines by the administrators. */
class FulfillmentAndPayoutTest {

    private static final Instant NOW = Instant.parse("2026-11-20T12:00:00Z");

    private final OrderItemRepository items = mock(OrderItemRepository.class);
    private final StoreEditorService editor = mock(StoreEditorService.class);
    private final NotificationService notifications = mock(NotificationService.class);
    private final FulfillmentService fulfillment = new FulfillmentService(items, editor, notifications);
    private final PayoutRepository payouts = mock(PayoutRepository.class);
    private final PayoutItemRepository payoutItems = mock(PayoutItemRepository.class);
    private final PayoutService payoutService = new PayoutService(payouts, payoutItems, items, notifications,
            Clock.fixed(NOW, ZoneOffset.UTC));

    private final Store store = TestEntities.store(5, TestEntities.master(70));
    private final Order order = new Order();
    private OrderItem first;
    private OrderItem second;

    @BeforeEach
    void paidOrderWithTwoLines() {
        store.getMaster().setUser(TestEntities.user(7, Language.ES));
        order.setOrderId(1L);
        order.setOrderCode("MUS-TEST");
        order.setStatus(OrderStatus.PAID);
        order.setBuyer(TestEntities.user(4, Language.EN));
        first = line(11, "108.00");
        second = line(12, "90.04");
        when(editor.myStore()).thenReturn(store);
        when(items.findByOrderOrderIdOrderByOrderItemIdAsc(1L)).thenReturn(List.of(first, second));
    }

    @Test
    void shippingAndDeliveringMovesTheOrderWhenEveryLineIsDone() {
        fulfillment.ship(11L, new ShipmentRequest("Servientrega", "SV-1"));
        assertThat(order.getStatus()).isEqualTo(OrderStatus.IN_PREPARATION);
        verify(notifications).notify(eq(order.getBuyer()), eq(FulfillmentService.ORDER_SHIPPED), anyString(),
                any(), eq("MUS-TEST"), eq("Servientrega"), eq("SV-1"));

        fulfillment.ship(12L, new ShipmentRequest("Servientrega", "SV-2"));
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);

        fulfillment.deliver(11L);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        fulfillment.deliver(12L);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    void stepsCannotBeSkippedOrRepeatedAndOtherStoresAreNotFound() {
        assertThatThrownBy(() -> fulfillment.deliver(11L)).hasMessage("fulfillment_step_invalid [PENDING]");
        fulfillment.ship(11L, new ShipmentRequest("Servientrega", "SV-1"));
        assertThatThrownBy(() -> fulfillment.ship(11L, new ShipmentRequest("x", "y")))
                .hasMessage("fulfillment_step_invalid [SHIPPED]");

        OrderItem foreign = line(13, "10.00");
        foreign.setStore(TestEntities.store(6, TestEntities.master(71)));
        assertThatThrownBy(() -> fulfillment.ship(13L, new ShipmentRequest("x", "y")))
                .hasMessage("order_item_not_found [13]");

        order.setStatus(OrderStatus.PENDING_PAYMENT);
        assertThatThrownBy(() -> fulfillment.ship(12L, new ShipmentRequest("x", "y")))
                .hasMessage("order_item_not_found [12]");
    }

    @Test
    void payoutsGroupDeliveredLinesPerMasterAndCurrencyAndArePaidOnce() {
        when(items.findDeliveredWithoutPayout(FulfillmentStatus.DELIVERED)).thenReturn(List.of(first, second));
        when(payouts.save(any(Payout.class))).thenAnswer(inv -> inv.getArgument(0));

        List<PayoutResponse> created = payoutService.createFromDeliveredLines();

        assertThat(created).singleElement().satisfies(p -> {
            assertThat(p.amount()).isEqualByComparingTo("198.04");
            assertThat(p.status()).isEqualTo(PayoutStatus.PENDING);
            assertThat(p.currency()).isEqualTo("USD");
        });
        verify(payoutItems, times(2)).save(any(PayoutItem.class));

        Payout payout = new Payout();
        payout.setPayoutId(3L);
        payout.setMaster(store.getMaster());
        payout.setStatus(PayoutStatus.PENDING);
        payout.setAmount(new BigDecimal("198.04"));
        payout.setCurrency("USD");
        when(payouts.findById(3L)).thenReturn(Optional.of(payout));

        assertThat(payoutService.markPaid(3L, " TRF-1 ").status()).isEqualTo(PayoutStatus.PAID);
        assertThat(payout.getReference()).isEqualTo("TRF-1");
        assertThat(payout.getPaidAt()).isEqualTo(NOW);
        assertThatThrownBy(() -> payoutService.markPaid(3L, "TRF-2")).hasMessage("payout_already_paid [3]");
    }

    private OrderItem line(long id, String net) {
        OrderItem item = new OrderItem();
        item.setOrderItemId(id);
        item.setOrder(order);
        item.setStore(store);
        item.setQuantity(1);
        item.setCurrency("USD");
        item.setLineTotal(new BigDecimal(net));
        item.setArtisanNetAmount(new BigDecimal(net));
        item.setFulfillmentStatus(FulfillmentStatus.PENDING);
        when(items.findById(id)).thenReturn(Optional.of(item));
        return item;
    }
}
