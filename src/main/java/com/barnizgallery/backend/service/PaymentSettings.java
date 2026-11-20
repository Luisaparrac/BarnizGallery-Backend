package com.barnizgallery.backend.service;

import org.springframework.stereotype.Component;

import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.BusinessRuleException;

import tools.jackson.databind.JsonNode;

/**
 * Payment settings from {@code app_settings}. {@code payments.test_mode} must be a boolean; when it is missing the
 * operation answers 409 naming it.
 */
@Component
public class PaymentSettings {

    static final String TEST_MODE = "payments.test_mode";

    private final AppSettingsService settings;

    public PaymentSettings(AppSettingsService settings) {
        this.settings = settings;
    }

    /** True while demo (simulated) payments are allowed. */
    public boolean testMode() {
        return settings.find(TEST_MODE)
                .filter(JsonNode::isBoolean)
                .map(JsonNode::booleanValue)
                .orElseThrow(() -> new BusinessRuleException(ErrorCode.SETTING_NOT_CONFIGURED, TEST_MODE));
    }
}
