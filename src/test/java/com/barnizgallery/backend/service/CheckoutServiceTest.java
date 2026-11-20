package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.orm.jpa.JpaSystemException;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.dto.CheckoutRequest;
import com.barnizgallery.backend.dto.OrderResponse;
import com.barnizgallery.backend.entity.Address;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.CartItem;
import com.barnizgallery.backend.entity.OrderItem;
import com.barnizgallery.backend.entity.Store;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.enums.MasterVerificationStatus;
import com.barnizgallery.backend.enums.SaleMode;
import com.barnizgallery.backend.enums.StoreStatus;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.repository.CartItemRepository;
import com.barnizgallery.backend.repository.OrderItemRepository;
import com.barnizgallery.backend.repository.OrderRepository;
import com.barnizgallery.backend.repository.PaymentObligationRepository;
import com.barnizgallery.backend.repository.UserRepository;
import com.barnizgallery.backend.security.CurrentUser;

/** Checkout: everything is checked before writing, and the database reservation decides the stock. */
class CheckoutServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-20T12:00:00Z");

    private final CartService cart = mock(CartService.class);
    private final AddressService addresses = mock(AddressService.class);
    private final CheckoutSettings settings = mock(CheckoutSettings.class);
    private final OrderCodeGenerator codes = mock(OrderCodeGenerator.class);
    private final OrderRepository orders = mock(OrderRepository.class);
    private final OrderItemRepository items = mock(OrderItemRepository.class);
    private final PaymentObligationRepository obligations = mock(PaymentObligationRepository.class);
    private final CartItemRepository cartItems = mock(CartItemRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final CurrentUser currentUser = mock(CurrentUser.class);
    private final CheckoutService service = new CheckoutService(cart, addresses, settings, codes, orders, items,
            obligations, cartItems, users, currentUser, Clock.fixed(NOW, ZoneOffset.UTC));

    private final Store store = TestEntities.store(5, TestEntities.master(70));

    @BeforeEach
    void defaults() {
        store.setStatus(StoreStatus.PUBLISHED);
        store.getMaster().setVerificationStatus(MasterVerificationStatus.APPROVED);
        User buyer = TestEntities.user(4, Language.ES);
        when(currentUser.requireId()).thenReturn(4L);
        when(users.getReferenceById(4L)).thenReturn(buyer);
        Address address = new Address();
        address.setAddressId(1L);
        address.setRecipientName("Cliente");
        when(addresses.myAddress(1L)).thenReturn(address);
        when(codes.next()).thenReturn("MUS-261120-TEST01");
        when(settings.commissionRate()).thenReturn(new BigDecimal("0.10"));
        when(settings.shipping("USD")).thenReturn(new BigDecimal("25"));
        when(settings.reservationMinutes()).thenReturn(15);
        when(items.saveAll(anyList())).thenAnswer(inv -> {
            List<OrderItem> saved = inv.getArgument(0);
            long id = 100;
            for (OrderItem item : saved) {
                item.setOrderItemId(id++);
            }
            return saved;
        });
    }

    @Test
    void checkoutBuildsTheOrderReservesEveryLineAndEmptiesTheCart() {
        List<CartItem> lines = List.of(line(artwork(8, "120", "USD", 1), 1), line(artwork(9, "33.35", "USD", 5), 3));
        when(cart.items()).thenReturn(lines);

        OrderResponse order = service.checkout(new CheckoutRequest(1L));

        assertThat(order.total()).isEqualByComparingTo("245.05");
        assertThat(order.paymentDueAt()).isEqualTo(NOW.plusSeconds(15 * 60));
        assertThat(order.obligations()).singleElement().satisfies(o -> assertThat(o.amount())
                .isEqualByComparingTo("245.05"));
        verify(items).reserve(100L, NOW.plusSeconds(15 * 60));
        verify(items).reserve(101L, NOW.plusSeconds(15 * 60));
        verify(cartItems).deleteAll(lines);
    }

    @Test
    void nothingIsWrittenWhenTheCartMixesCurrenciesOrASettingIsMissing() {
        when(cart.items()).thenReturn(List.of(line(artwork(8, "120", "USD", 1), 1),
                line(artwork(10, "250000", "COP", 2), 1)));
        assertThatThrownBy(() -> service.checkout(new CheckoutRequest(1L)))
                .hasMessage("cart_mixed_currencies [USD, COP]");

        when(cart.items()).thenReturn(List.of(line(artwork(8, "120", "USD", 1), 1)));
        when(settings.reservationMinutes())
                .thenThrow(new BusinessRuleException(ErrorCode.SETTING_NOT_CONFIGURED, "cart.reservation_minutes"));
        assertThatThrownBy(() -> service.checkout(new CheckoutRequest(1L)))
                .hasMessage("setting_not_configured [cart.reservation_minutes]");

        when(cart.items()).thenReturn(List.of());
        assertThatThrownBy(() -> service.checkout(new CheckoutRequest(1L))).hasMessage("cart_empty");
        verify(orders, never()).save(any());
    }

    @Test
    void aLineThatIsNoLongerForSaleOrAskedAboveStockStopsTheCheckout() {
        Artwork archived = artwork(8, "120", "USD", 1);
        archived.setStatus(ArtworkStatus.ARCHIVED);
        when(cart.items()).thenReturn(List.of(line(archived, 1)));
        assertThatThrownBy(() -> service.checkout(new CheckoutRequest(1L))).hasMessage("artwork_not_for_sale [8]");

        when(cart.items()).thenReturn(List.of(line(artwork(9, "10", "USD", 2), 3)));
        assertThatThrownBy(() -> service.checkout(new CheckoutRequest(1L))).hasMessage("not_enough_stock [2]");
        verify(orders, never()).save(any());
    }

    @Test
    void aReservationRefusedByTheDatabaseIsA409() {
        when(cart.items()).thenReturn(List.of(line(artwork(8, "120", "USD", 1), 1)));
        when(items.reserve(eq(100L), any())).thenThrow(new JpaSystemException(
                new RuntimeException(new SQLException("Existencias insuficientes", "P0001"))));

        assertThatThrownBy(() -> service.checkout(new CheckoutRequest(1L))).hasMessage("reservation_rejected [8]");
        verify(cartItems, never()).deleteAll(anyList());
    }

    private Artwork artwork(long id, String price, String currency, int stock) {
        Artwork artwork = TestEntities.artwork(id, store, ArtworkStatus.EXHIBITED);
        artwork.setSaleMode(SaleMode.SALE);
        artwork.setPrice(new BigDecimal(price));
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
