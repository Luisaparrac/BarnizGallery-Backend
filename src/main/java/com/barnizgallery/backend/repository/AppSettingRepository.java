package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.AppSetting;

/**
 * Spring Data repository for {@link AppSetting}.
 */
public interface AppSettingRepository extends JpaRepository<AppSetting, String> {
}
