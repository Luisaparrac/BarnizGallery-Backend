package com.barnizgallery.backend.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Virtual room dedicated to one master (1:1). Maps the existing table {@code rooms}.
 */
@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Integer roomId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "master_id", nullable = false, unique = true)
    private Master master;

    @Column(name = "name_es", nullable = false, length = 150)
    private String nameEs;

    @Column(name = "name_en", nullable = false, length = 150)
    private String nameEn;

    @Column(name = "description_es", columnDefinition = "text")
    private String descriptionEs;

    @Column(name = "description_en", columnDefinition = "text")
    private String descriptionEn;

    @Column(name = "scene_3d_url", length = 255)
    private String scene3dUrl;

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public Master getMaster() {
        return master;
    }

    public void setMaster(Master master) {
        this.master = master;
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

    public String getScene3dUrl() {
        return scene3dUrl;
    }

    public void setScene3dUrl(String scene3dUrl) {
        this.scene3dUrl = scene3dUrl;
    }
}
