package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.barnizgallery.backend.MutableClock;
import com.barnizgallery.backend.entity.AppSetting;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.repository.AppSettingRepository;

class AppSettingsServiceTest {

    private final AppSettingRepository repository = mock(AppSettingRepository.class);
    private final MutableClock clock = new MutableClock(Instant.parse("2026-11-19T12:00:00Z"));
    private final AppSettingsService settings = new AppSettingsService(repository, clock);

    @BeforeEach
    void setUp() {
        when(repository.findAll()).thenReturn(List.of(
                setting("currency.default", "\"USD\""),
                setting("platform.commission_rate", "0.10"),
                setting("auction.payment_hours", "48"),
                setting("auction.min_increment", "null"),
                setting("payments.test_mode", "true"),
                setting("broken", "{not json")));
    }

    @Test
    void readsTypedValues() {
        assertThat(settings.text("currency.default")).isEqualTo("USD");
        assertThat(settings.decimal("platform.commission_rate")).isEqualByComparingTo("0.10");
        assertThat(settings.integer("auction.payment_hours")).isEqualTo(48);
    }

    @Test
    void nullOrMissingSettingsAre409NamingTheSetting() {
        assertThatThrownBy(() -> settings.decimal("auction.min_increment"))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT))
                .hasMessage("setting_not_configured [auction.min_increment]");
        assertThatThrownBy(() -> settings.integer("does.not.exist"))
                .hasMessage("setting_not_configured [does.not.exist]");
        assertThatThrownBy(() -> settings.text("broken")).hasMessage("setting_not_configured [broken]");
        assertThat(settings.find("auction.min_increment")).isEmpty();
    }

    @Test
    void valuesOfTheWrongTypeAre409() {
        assertThatThrownBy(() -> settings.decimal("currency.default")).hasMessage("setting_invalid [currency.default]");
        assertThatThrownBy(() -> settings.integer("platform.commission_rate"))
                .hasMessage("setting_invalid [platform.commission_rate]");
        assertThatThrownBy(() -> settings.text("payments.test_mode")).hasMessage("setting_invalid [payments.test_mode]");
    }

    @Test
    void valuesAreCachedForAShortTime() {
        settings.text("currency.default");
        settings.decimal("platform.commission_rate");
        clock.advance(Duration.ofSeconds(29));
        settings.text("currency.default");
        verify(repository, times(1)).findAll();

        clock.advance(Duration.ofSeconds(2));
        settings.text("currency.default");
        verify(repository, times(2)).findAll();

        settings.invalidate();
        settings.text("currency.default");
        verify(repository, times(3)).findAll();
    }

    private static AppSetting setting(String key, String json) {
        AppSetting setting = new AppSetting();
        setting.setSettingKey(key);
        setting.setValue(json);
        return setting;
    }
}
