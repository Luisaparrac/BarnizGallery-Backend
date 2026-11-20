package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.barnizgallery.backend.config.PayPalProperties;
import com.barnizgallery.backend.config.PaymentConfig;
import com.barnizgallery.backend.config.WompiProperties;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.exception.FeatureDisabledException;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.patterns.adapter.SimulatedGateway;

class PaymentGatewayRegistryTest {

    private final PaymentConfig config = new PaymentConfig();

    @Test
    void choosesTheGatewayByCurrency() {
        PaymentGatewayRegistry registry = registry(true);

        assertThat(registry.forCurrency("COP").code()).isEqualTo("wompi");
        assertThat(registry.forCurrency("usd").code()).isEqualTo("paypal");
        assertThat(registry.simulated().code()).isEqualTo("simulated");
        assertThat(registry.byCode("paypal").currencies()).containsExactly("USD");
    }

    @Test
    void unsupportedCurrencyIs409() {
        assertThatThrownBy(() -> registry(true).forCurrency("EUR"))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT))
                .hasMessage("payment_currency_not_supported [EUR]");
    }

    @Test
    void gatewayWithoutCredentialsIs503() {
        assertThatThrownBy(() -> registry(false).forCurrency("COP")).isInstanceOf(FeatureDisabledException.class)
                .hasMessage("payment_gateway_disabled [COP]");
        assertThatThrownBy(() -> registry(false).forCurrency("USD")).isInstanceOf(FeatureDisabledException.class);
    }

    @Test
    void unknownGatewayCodeIs404() {
        assertThatThrownBy(() -> registry(true).byCode("stripe")).isInstanceOf(ResourceNotFoundException.class);
    }

    private PaymentGatewayRegistry registry(boolean configured) {
        String secret = configured ? UUID.randomUUID().toString() : "";
        WompiProperties wompi = new WompiProperties(configured ? "pub_test_x" : "", secret, secret,
                "https://checkout.wompi.co/p/");
        PayPalProperties paypal = new PayPalProperties(configured ? "client" : "", secret, configured ? "WH-1" : "",
                "https://paypal.test", Duration.ofSeconds(15));
        return new PaymentGatewayRegistry(List.of(config.wompiGateway(wompi),
                config.payPalGateway(paypal, Clock.systemUTC()), new SimulatedGateway("https://museo.test")));
    }
}
