package com.barnizgallery.backend.model.entity;

import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.barnizgallery.backend.model.converter.ArtworkStatusConverter;
import com.barnizgallery.backend.model.enums.ArtworkStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A piece exhibited in a room. Maps the existing table {@code artworks}.
 */
@Entity
@Table(name = "artworks")
public class Artwork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "artwork_id")
    private Integer artworkId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "title_es", nullable = false, length = 150)
    private String titleEs;

    @Column(name = "title_en", nullable = false, length = 150)
    private String titleEn;

    @Column(name = "history_es", columnDefinition = "text")
    private String historyEs;

    @Column(name = "history_en", columnDefinition = "text")
    private String historyEn;

    @Column(name = "technique", length = 150)
    private String technique;

    @Column(name = "dimensions", length = 100)
    private String dimensions;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "color_tags", columnDefinition = "text[]")
    private List<String> colorTags;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "motif_tags", columnDefinition = "text[]")
    private List<String> motifTags;

    @Convert(converter = ArtworkStatusConverter.class)
    @Column(name = "status", nullable = false, length = 30)
    private ArtworkStatus status;

    public Integer getArtworkId() {
        return artworkId;
    }

    public void setArtworkId(Integer artworkId) {
        this.artworkId = artworkId;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public String getTitleEs() {
        return titleEs;
    }

    public void setTitleEs(String titleEs) {
        this.titleEs = titleEs;
    }

    public String getTitleEn() {
        return titleEn;
    }

    public void setTitleEn(String titleEn) {
        this.titleEn = titleEn;
    }

    public String getHistoryEs() {
        return historyEs;
    }

    public void setHistoryEs(String historyEs) {
        this.historyEs = historyEs;
    }

    public String getHistoryEn() {
        return historyEn;
    }

    public void setHistoryEn(String historyEn) {
        this.historyEn = historyEn;
    }

    public String getTechnique() {
        return technique;
    }

    public void setTechnique(String technique) {
        this.technique = technique;
    }

    public String getDimensions() {
        return dimensions;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

    public List<String> getColorTags() {
        return colorTags;
    }

    public void setColorTags(List<String> colorTags) {
        this.colorTags = colorTags;
    }

    public List<String> getMotifTags() {
        return motifTags;
    }

    public void setMotifTags(List<String> motifTags) {
        this.motifTags = motifTags;
    }

    public ArtworkStatus getStatus() {
        return status;
    }

    public void setStatus(ArtworkStatus status) {
        this.status = status;
    }
}
