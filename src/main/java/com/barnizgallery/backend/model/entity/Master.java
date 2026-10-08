package com.barnizgallery.backend.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A master artisan of Barniz de Pasto. Maps the existing table {@code masters}.
 */
@Entity
@Table(name = "masters")
public class Master {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "master_id")
    private Integer masterId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "biography_es", columnDefinition = "text")
    private String biographyEs;

    @Column(name = "biography_en", columnDefinition = "text")
    private String biographyEn;

    @Column(name = "workshop", length = 150)
    private String workshop;

    @Column(name = "contact_info", length = 200)
    private String contactInfo;

    public Integer getMasterId() {
        return masterId;
    }

    public void setMasterId(Integer masterId) {
        this.masterId = masterId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBiographyEs() {
        return biographyEs;
    }

    public void setBiographyEs(String biographyEs) {
        this.biographyEs = biographyEs;
    }

    public String getBiographyEn() {
        return biographyEn;
    }

    public void setBiographyEn(String biographyEn) {
        this.biographyEn = biographyEn;
    }

    public String getWorkshop() {
        return workshop;
    }

    public void setWorkshop(String workshop) {
        this.workshop = workshop;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }
}
