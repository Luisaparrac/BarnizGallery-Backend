package com.barnizgallery.backend.entity;

import com.barnizgallery.backend.converter.CameraModeConverter;
import com.barnizgallery.backend.converter.LanguageConverter;
import com.barnizgallery.backend.enums.CameraMode;
import com.barnizgallery.backend.enums.Language;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * User of the gallery, identified only by email. Maps the existing table {@code users}.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "country", length = 100)
    private String country;

    @Convert(converter = LanguageConverter.class)
    @Column(name = "preferred_language", nullable = false, length = 10)
    private Language preferredLanguage;

    @Convert(converter = CameraModeConverter.class)
    @Column(name = "camera_mode", nullable = false, length = 30)
    private CameraMode cameraMode;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Language getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(Language preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public CameraMode getCameraMode() {
        return cameraMode;
    }

    public void setCameraMode(CameraMode cameraMode) {
        this.cameraMode = cameraMode;
    }
}
