package com.barnizgallery.backend;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;

import com.barnizgallery.backend.service.AppSettingsService;

/**
 * Mocked {@link AppSettingsService} for unit tests, with the auction rules already set.
 */
public final class TestSettings {

    private TestSettings() {
    }

    /** Currency USD, minimum increment 1, at most 5 bids per minute and suspicious above 10 times. */
    public static AppSettingsService auctionRules() {
        AppSettingsService settings = mock(AppSettingsService.class);
        lenient().when(settings.text(AppSettingsService.CURRENCY_DEFAULT)).thenReturn("USD");
        lenient().when(settings.decimal(AppSettingsService.AUCTION_MIN_INCREMENT)).thenReturn(BigDecimal.ONE);
        lenient().when(settings.integer(AppSettingsService.AUCTION_MAX_BIDS_PER_MINUTE)).thenReturn(5);
        lenient().when(settings.decimal(AppSettingsService.AUCTION_SUSPICIOUS_MULTIPLIER)).thenReturn(BigDecimal.TEN);
        return settings;
    }
}
