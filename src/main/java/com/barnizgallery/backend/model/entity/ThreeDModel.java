package com.barnizgallery.backend.model.entity;

import java.time.LocalDateTime;

import com.barnizgallery.backend.model.converter.GenerationStatusConverter;
import com.barnizgallery.backend.model.enums.GenerationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * 3D model (GLB) generated for an artwork (1:1). Maps the existing table {@code three_d_models}.
 */
@Entity
@Table(name = "three_d_models")
public class ThreeDModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "model_id")
    private Integer modelId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "artwork_id", nullable = false, unique = true)
    private Artwork artwork;

    @Column(name = "hyper3d_task_id", length = 150)
    private String hyper3dTaskId;

    @Convert(converter = GenerationStatusConverter.class)
    @Column(name = "generation_status", nullable = false, length = 30)
    private GenerationStatus generationStatus;

    @Column(name = "glb_file_url", length = 255)
    private String glbFileUrl;

    @Column(name = "generation_date")
    private LocalDateTime generationDate;

    /** The column has a DB default, but the value is set from Java so we do not depend on it. */
    @PrePersist
    void onCreate() {
        if (generationDate == null) {
            generationDate = LocalDateTime.now();
        }
    }

    public Integer getModelId() {
        return modelId;
    }

    public void setModelId(Integer modelId) {
        this.modelId = modelId;
    }

    public Artwork getArtwork() {
        return artwork;
    }

    public void setArtwork(Artwork artwork) {
        this.artwork = artwork;
    }

    public String getHyper3dTaskId() {
        return hyper3dTaskId;
    }

    public void setHyper3dTaskId(String hyper3dTaskId) {
        this.hyper3dTaskId = hyper3dTaskId;
    }

    public GenerationStatus getGenerationStatus() {
        return generationStatus;
    }

    public void setGenerationStatus(GenerationStatus generationStatus) {
        this.generationStatus = generationStatus;
    }

    public String getGlbFileUrl() {
        return glbFileUrl;
    }

    public void setGlbFileUrl(String glbFileUrl) {
        this.glbFileUrl = glbFileUrl;
    }

    public LocalDateTime getGenerationDate() {
        return generationDate;
    }

    public void setGenerationDate(LocalDateTime generationDate) {
        this.generationDate = generationDate;
    }
}
