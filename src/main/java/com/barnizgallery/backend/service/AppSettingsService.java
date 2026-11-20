package com.barnizgallery.backend.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.entity.AppSetting;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.repository.AppSettingRepository;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Business settings from {@code app_settings} (commission, shipping, auction rules...), kept in memory for a
 * short time ({@value #TTL_SECONDS} s) so each request does not read the table. A setting whose value is JSON
 * {@code null} is "not configured yet": the backend never invents a default for it, and an operation that needs
 * it answers 409 naming the missing setting.
 */
@Service
public class AppSettingsService {

    public static final String CURRENCY_DEFAULT = "currency.default";
    public static final String AUCTION_MIN_INCREMENT = "auction.min_increment";
    public static final String AUCTION_MAX_BIDS_PER_MINUTE = "auction.max_bids_per_minute";
    public static final String AUCTION_SUSPICIOUS_MULTIPLIER = "auction.suspicious_multiplier";

    static final long TTL_SECONDS = 30;

    private static final Logger log = LoggerFactory.getLogger(AppSettingsService.class);
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final AppSettingRepository repository;
    private final Clock clock;
    private Map<String, JsonNode> values = Map.of();
    private Instant loadedAt = Instant.MIN;

    public AppSettingsService(AppSettingRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    /** The value of a setting, or empty when the row does not exist or its value is JSON null. */
    @Transactional(readOnly = true)
    public Optional<JsonNode> find(String key) {
        JsonNode value = snapshot().get(key);
        return value == null || value.isNull() ? Optional.empty() : Optional.of(value);
    }

    /** A number setting (for example {@code platform.commission_rate}). */
    @Transactional(readOnly = true)
    public BigDecimal decimal(String key) {
        JsonNode value = require(key);
        if (!value.isNumber()) {
            throw new BusinessRuleException(ErrorCode.SETTING_INVALID, key);
        }
        return value.decimalValue();
    }

    /** A whole-number setting (for example {@code auction.max_bids_per_minute}). */
    @Transactional(readOnly = true)
    public int integer(String key) {
        JsonNode value = require(key);
        if (!value.isIntegralNumber() || !value.canConvertToInt()) {
            throw new BusinessRuleException(ErrorCode.SETTING_INVALID, key);
        }
        return value.intValue();
    }

    /** A text setting (for example {@code currency.default}). */
    @Transactional(readOnly = true)
    public String text(String key) {
        JsonNode value = require(key);
        if (!value.isString() || value.asString().isBlank()) {
            throw new BusinessRuleException(ErrorCode.SETTING_INVALID, key);
        }
        return value.asString();
    }

    /** The value, or 409 {@code setting_not_configured} naming the setting when it is missing or JSON null. */
    private JsonNode require(String key) {
        return find(key).orElseThrow(() -> new BusinessRuleException(ErrorCode.SETTING_NOT_CONFIGURED, key));
    }

    /** Forgets the cached values, so the next read sees a change made right now. */
    public synchronized void invalidate() {
        loadedAt = Instant.MIN;
    }

    private synchronized Map<String, JsonNode> snapshot() {
        Instant now = clock.instant();
        if (now.isBefore(loadedAt.plus(Duration.ofSeconds(TTL_SECONDS)))) {
            return values;
        }
        Map<String, JsonNode> fresh = new HashMap<>();
        for (AppSetting setting : repository.findAll()) {
            fresh.put(setting.getSettingKey(), parse(setting));
        }
        values = Map.copyOf(fresh);
        loadedAt = now;
        return values;
    }

    private static JsonNode parse(AppSetting setting) {
        try {
            return JSON.readTree(setting.getValue());
        } catch (JacksonException ex) {
            log.warn("Setting {} is not valid JSON and is ignored", setting.getSettingKey());
            return JSON.nullNode();
        }
    }
}
