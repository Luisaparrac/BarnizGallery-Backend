package com.barnizgallery.backend.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.BusinessRuleException;

/**
 * Settings the checkout needs, read from {@code app_settings}. A missing value answers 409 naming the setting;
 * nothing is defaulted here.
 * <p>
 * Shipping: {@code shipping.fixed_amount.<CURRENCY>} when that setting exists; otherwise
 * {@code shipping.fixed_amount}, which is expressed in {@code currency.default} and therefore only applies to
 * orders in that currency.
 */
@Component
public class CheckoutSettings {

    static final String COMMISSION_RATE = "platform.commission_rate";
    static final String SHIPPING_FIXED_AMOUNT = "shipping.fixed_amount";

    private final AppSettingsService settings;

    public CheckoutSettings(AppSettingsService settings) {
        this.settings = settings;
    }

    public BigDecimal commissionRate() {
        return settings.decimal(COMMISSION_RATE);
    }

    public BigDecimal shipping(String currency) {
        String perCurrency = SHIPPING_FIXED_AMOUNT + "." + currency;
        if (settings.find(perCurrency).isPresent()) {
            return settings.decimal(perCurrency);
        }
        if (currency.equals(settings.text(AppSettingsService.CURRENCY_DEFAULT))) {
            return settings.decimal(SHIPPING_FIXED_AMOUNT);
        }
        throw new BusinessRuleException(ErrorCode.SETTING_NOT_CONFIGURED, perCurrency);
    }
}
