package com.barnizgallery.backend.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Template a store is created from: default layout (sections) and theme as JSON.
 * Maps the table {@code museo.store_templates}.
 */
@Entity
@Table(name = "store_templates")
public class StoreTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "name_es", nullable = false)
    private String nameEs;

    @Column(name = "name_en", nullable = false)
    private String nameEn;

    @Column(name = "description_es")
    private String descriptionEs;

    @Column(name = "description_en")
    private String descriptionEn;

    @Column(name = "preview_url")
    private String previewUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "default_layout", nullable = false, columnDefinition = "jsonb")
    private String defaultLayout = "{}";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "default_theme", nullable = false, columnDefinition = "jsonb")
    private String defaultTheme = "{}";

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNameEs() {
        return nameEs;
    }

    public void setNameEs(String nameEs) {
        this.nameEs = nameEs;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getDescriptionEs() {
        return descriptionEs;
    }

    public void setDescriptionEs(String descriptionEs) {
        this.descriptionEs = descriptionEs;
    }

    public String getDescriptionEn() {
        return descriptionEn;
    }

    public void setDescriptionEn(String descriptionEn) {
        this.descriptionEn = descriptionEn;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public String getDefaultLayout() {
        return defaultLayout;
    }

    public void setDefaultLayout(String defaultLayout) {
        this.defaultLayout = defaultLayout;
    }

    public String getDefaultTheme() {
        return defaultTheme;
    }

    public void setDefaultTheme(String defaultTheme) {
        this.defaultTheme = defaultTheme;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
