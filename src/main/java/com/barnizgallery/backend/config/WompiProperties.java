package com.barnizgallery.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code app.payments.wompi.*} properties. Wompi charges only COP. Without the public key and both secrets the
 * gateway stays disabled. Test keys ({@code pub_test_...}) use the Wompi sandbox.
 *
 * @param publicKey       {@code WOMPI_PUBLIC_KEY}, sent to the Web Checkout
 * @param integritySecret {@code WOMPI_INTEGRITY_SECRET}, signs each checkout; never sent
 * @param eventsSecret    {@code WOMPI_EVENTS_SECRET}, checks the event checksums; never sent
 * @param checkoutUrl     {@code WOMPI_BASE_URL}, the Web Checkout page
 */
@ConfigurationProperties(prefix = "app.payments.wompi")
public record WompiProperties(String publicKey, String integritySecret, String eventsSecret, String checkoutUrl) {

    /** True only when the public key and both secrets are set. */
    public boolean isConfigured() {
        return notBlank(publicKey) && notBlank(integritySecret) && notBlank(eventsSecret);
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
