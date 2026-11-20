package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.entity.FavoriteId;
import com.barnizgallery.backend.entity.Order;
import com.barnizgallery.backend.entity.Payment;
import com.barnizgallery.backend.entity.PaymentObligation;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.AuthTokenPurpose;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.enums.OrderStatus;
import com.barnizgallery.backend.enums.PaymentObligationStatus;
import com.barnizgallery.backend.enums.PaymentStatus;
import com.barnizgallery.backend.repository.FavoriteRepository;
import com.barnizgallery.backend.repository.OrderRepository;
import com.barnizgallery.backend.repository.PaymentObligationRepository;
import com.barnizgallery.backend.repository.PaymentRepository;
import com.barnizgallery.backend.repository.UserRepository;
import com.barnizgallery.backend.security.CurrentUser;

/** Services behind the routes the frontend uses: password change, order cancel, payment detail and favorites. */
class FrontendRoutesTest {

    private final CurrentUser currentUser = mock(CurrentUser.class);
    private final User buyer = TestEntities.user(4, Language.ES);

    @Test
    void thePasswordChangesOnlyWithTheCurrentOneAndClosesOtherSessions() {
        UserRepository users = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        AuthTokenService tokens = mock(AuthTokenService.class);
        buyer.setPasswordHash("old-hash");
        when(currentUser.requireId()).thenReturn(4L);
        when(users.findById(4L)).thenReturn(Optional.of(buyer));
        when(encoder.matches("bien-1234", "old-hash")).thenReturn(true);
        when(encoder.encode("nueva-1234")).thenReturn("new-hash");
        PasswordChangeService service = new PasswordChangeService(users, encoder, tokens, currentUser);

        assertThatThrownBy(() -> service.change("mal-1234", "nueva-1234")).hasMessage("wrong_password");
        service.change("bien-1234", "nueva-1234");

        assertThat(buyer.getPasswordHash()).isEqualTo("new-hash");
        verify(tokens).revokeAll(buyer, AuthTokenPurpose.REFRESH);
    }

    @Test
    void onlyAnOrderWaitingForPaymentWithoutAPendingPaymentIsCancelled() {
        OrderQueryService orders = mock(OrderQueryService.class);
        OrderRepository orderRepository = mock(OrderRepository.class);
        PaymentObligationRepository obligations = mock(PaymentObligationRepository.class);
        PaymentRepository payments = mock(PaymentRepository.class);
        Order order = order();
        PaymentObligation obligation = obligation(order);
        when(orders.myOrder(1L)).thenReturn(order);
        when(obligations.findByOrderOrderIdOrderByObligationIdAsc(1L)).thenReturn(List.of(obligation));
        OrderCancelService service = new OrderCancelService(orders, orderRepository, obligations, payments);

        when(payments.existsByObligationObligationIdAndStatus(10L, PaymentStatus.PENDING)).thenReturn(true);
        assertThatThrownBy(() -> service.cancel(1L)).hasMessage("payment_already_pending [MUS-TEST]");
        verify(orderRepository, never()).releaseReservations(any());

        when(payments.existsByObligationObligationIdAndStatus(10L, PaymentStatus.PENDING)).thenReturn(false);
        service.cancel(1L);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(obligation.getStatus()).isEqualTo(PaymentObligationStatus.CANCELLED);
        verify(orderRepository).releaseReservations(1L);
        assertThatThrownBy(() -> service.cancel(1L)).hasMessage("order_not_cancellable [MUS-TEST]");
    }

    @Test
    void aPaymentOfSomeoneElseIsNotFound() {
        PaymentRepository payments = mock(PaymentRepository.class);
        Payment payment = new Payment();
        payment.setPaymentId(7L);
        payment.setObligation(obligation(order()));
        payment.setGateway("simulated");
        payment.setStatus(PaymentStatus.APPROVED);
        payment.setAmount(BigDecimal.TEN);
        payment.setCurrency("USD");
        when(payments.findById(7L)).thenReturn(Optional.of(payment));
        PaymentQueryService service = new PaymentQueryService(payments, currentUser);

        when(currentUser.requireId()).thenReturn(4L);
        assertThat(service.mine(7L).gateway()).isEqualTo("SIMULATED");
        assertThat(service.mine(7L).reference()).isEqualTo("PAY-7");
        when(currentUser.requireId()).thenReturn(5L);
        assertThatThrownBy(() -> service.mine(7L)).hasMessage("payment_not_found [7]");
    }

    @Test
    void toggleAddsAndRemovesAFavorite() {
        FavoriteRepository favorites = mock(FavoriteRepository.class);
        ArtworkService artworks = mock(ArtworkService.class);
        when(currentUser.requireId()).thenReturn(4L);
        when(artworks.requirePublic(9L)).thenReturn(
                TestEntities.artwork(9, TestEntities.store(1, TestEntities.master(1)),
                        ArtworkStatus.EXHIBITED));
        FavoriteService service = new FavoriteService(favorites, mock(UserRepository.class), artworks, currentUser);

        when(favorites.existsById(new FavoriteId(4L, 9L))).thenReturn(false);
        assertThat(service.toggle(9L).favorite()).isTrue();
        when(favorites.existsById(new FavoriteId(4L, 9L))).thenReturn(true);
        assertThat(service.toggle(9L).favorite()).isFalse();
        verify(favorites).deleteById(new FavoriteId(4L, 9L));
    }

    private Order order() {
        Order order = new Order();
        order.setOrderId(1L);
        order.setOrderCode("MUS-TEST");
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setBuyer(buyer);
        return order;
    }

    private static PaymentObligation obligation(Order order) {
        PaymentObligation obligation = new PaymentObligation();
        obligation.setObligationId(10L);
        obligation.setOrder(order);
        obligation.setStatus(PaymentObligationStatus.PENDING);
        return obligation;
    }
}
