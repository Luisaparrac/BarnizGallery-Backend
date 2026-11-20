package com.barnizgallery.backend.service;

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
import com.barnizgallery.backend.repository.AppSettingRepository;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Business settings from {@code app_settings} (commission, shipping, auction rules...), kept in memory for a
 * short time ({@value #TTL_SECONDS} s) so each request does not read the table. A setting whose value is JSON
 * {@code null} is "not configured yet": the backend never invents a default for it.
 */
@Service
public class AppSettingsService {

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
