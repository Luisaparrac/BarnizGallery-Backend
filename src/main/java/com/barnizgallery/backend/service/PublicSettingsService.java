package com.barnizgallery.backend.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.JsonNode;

/**
 * The settings the frontend may read without logging in. Only an explicit list of harmless values is exposed:
 * default currency, languages, minimum bid increment, cart reservation minutes and shipping amounts. Gateways, AI,
 * commissions and anything internal never leave the backend. A setting that is not configured is sent as null.
 */
@Service
public class PublicSettingsService {

    static final List<String> PUBLIC_KEYS = List.of(AppSettingsService.CURRENCY_DEFAULT, "languages.available",
            AppSettingsService.AUCTION_MIN_INCREMENT, CheckoutSettings.RESERVATION_MINUTES,
            CheckoutSettings.SHIPPING_FIXED_AMOUNT);

    private final AppSettingsService settings;

    public PublicSettingsService(AppSettingsService settings) {
        this.settings = settings;
    }

    @Transactional(readOnly = true)
    public Map<String, JsonNode> values() {
        Map<String, JsonNode> values = new LinkedHashMap<>();
        PUBLIC_KEYS.forEach(key -> values.put(key, settings.find(key).orElse(null)));
        settings.keysStartingWith(CheckoutSettings.SHIPPING_FIXED_AMOUNT + ".")
                .forEach(key -> values.put(key, settings.find(key).orElse(null)));
        return values;
    }
}
