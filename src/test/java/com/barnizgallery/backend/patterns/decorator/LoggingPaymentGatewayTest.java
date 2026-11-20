package com.barnizgallery.backend.patterns.decorator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.barnizgallery.backend.enums.PaymentStatus;
import com.barnizgallery.backend.patterns.adapter.CheckoutRequest;
import com.barnizgallery.backend.patterns.adapter.CheckoutSession;
import com.barnizgallery.backend.patterns.adapter.GatewayEvent;
import com.barnizgallery.backend.patterns.adapter.PaymentGateway;
import com.barnizgallery.backend.patterns.adapter.WebhookCall;

class LoggingPaymentGatewayTest {

    private final PaymentGateway wompi = mock(PaymentGateway.class);
    private final LoggingPaymentGateway decorated = new LoggingPaymentGateway(wompi);

    @Test
    void itDelegatesEveryCallAndKeepsTheResults() {
        when(wompi.code()).thenReturn("wompi");
        when(wompi.currencies()).thenReturn(Set.of("COP"));
        CheckoutRequest request = new CheckoutRequest("PAY-1", BigDecimal.TEN, "COP", null, "MUSEO", "u", "c", null);
        CheckoutSession session = new CheckoutSession("wompi", "PAY-1", "https://checkout.test");
        when(wompi.startCheckout(request)).thenReturn(session);
        WebhookCall call = new WebhookCall(Map.of(), "{}");
        GatewayEvent event = new GatewayEvent("wompi", "e1", "PAY-1", "t1", PaymentStatus.APPROVED, "{}");
        when(wompi.readEvent(call)).thenReturn(Optional.of(event));

        assertThat(decorated.code()).isEqualTo("wompi");
        assertThat(decorated.currencies()).containsExactly("COP");
        assertThat(decorated.startCheckout(request)).isSameAs(session);
        assertThat(decorated.readEvent(call)).contains(event);
        assertThat(decorated.wrapped()).isSameAs(wompi);
    }

    @Test
    void failuresOfTheWrappedGatewayReachTheCaller() {
        WebhookCall call = new WebhookCall(Map.of(), "{}");
        when(wompi.readEvent(call)).thenThrow(new IllegalStateException("bad signature"));

        assertThatThrownBy(() -> decorated.readEvent(call)).hasMessage("bad signature");
        verify(wompi).readEvent(call);
    }
}
