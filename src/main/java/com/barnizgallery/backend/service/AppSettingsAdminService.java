package com.barnizgallery.backend.service;

import java.time.Clock;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.AppSettingResponse;
import com.barnizgallery.backend.entity.AppSetting;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.repository.AppSettingRepository;
import com.barnizgallery.backend.repository.UserRepository;
import com.barnizgallery.backend.security.CurrentUser;

import tools.jackson.databind.JsonNode;

/**
 * Administrators read and change the existing settings. Settings are not created or deleted from the API; every
 * change is recorded by the database audit with the administrator's id, and the settings cache is cleared.
 */
@Service
public class AppSettingsAdminService {

    private final AppSettingRepository repository;
    private final AppSettingsService settings;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;
    private final Clock clock;

    public AppSettingsAdminService(AppSettingRepository repository, AppSettingsService settings,
            UserRepository userRepository, CurrentUser currentUser, Clock clock) {
        this.repository = repository;
        this.settings = settings;
        this.userRepository = userRepository;
        this.currentUser = currentUser;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<AppSettingResponse> findAll() {
        return repository.findAll(Sort.by("settingKey")).stream().map(AppSettingsAdminService::toResponse).toList();
    }

    @Transactional
    public AppSettingResponse update(String key, JsonNode value) {
        AppSetting setting = repository.findById(key)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SETTING_NOT_FOUND, key));
        setting.setValue(value.toString());
        setting.setUpdatedBy(userRepository.getReferenceById(currentUser.requireId()));
        setting.setUpdatedAt(clock.instant());
        repository.flush();
        settings.invalidate();
        return toResponse(setting);
    }

    private static AppSettingResponse toResponse(AppSetting setting) {
        return new AppSettingResponse(setting.getSettingKey(), setting.getValue(), setting.getDescription(),
                setting.isPublicSetting(), setting.getUpdatedAt());
    }
}
