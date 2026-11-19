package com.barnizgallery.backend.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.barnizgallery.backend.converter.ArtworkStatusConverter;
import com.barnizgallery.backend.converter.SaleModeConverter;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.SaleMode;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A piece of a store. Maps the table {@code museo.artworks}.
 * Migrated artworks keep their v1 dimensions text and have no price, stock, shape or style yet.
 */
@Entity
@Table(name = "artworks")
public class Artwork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "artwork_id")
    private Long artworkId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "title_es", nullable = false, length = 150)
    private String titleEs;

    @Column(name = "title_en", nullable = false, length = 150)
    private String titleEn;

    @Column(name = "history_es")
    private String historyEs;

    @Column(name = "history_en")
    private String historyEn;

    @Column(name = "technique", length = 150)
    private String technique;

    @Column(name = "slug")
    private String slug;

    @Column(name = "dimensions_legacy")
    private String dimensionsLegacy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shape_id")
    private Shape shape;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "style_id")
    private Style style;

    @Column(name = "width_cm", precision = 8, scale = 2)
    private BigDecimal widthCm;

    @Column(name = "height_cm", precision = 8, scale = 2)
    private BigDecimal heightCm;

    @Column(name = "depth_cm", precision = 8, scale = 2)
    private BigDecimal depthCm;

    @Column(name = "weight_g", precision = 10, scale = 2)
    private BigDecimal weightG;

    @Convert(converter = SaleModeConverter.class)
    @Column(name = "sale_mode", length = 20)
    private SaleMode saleMode;

    @Column(name = "price", precision = 14, scale = 2)
    private BigDecimal price;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "stock")
    private Integer stock;

    @ManyToMany
    @JoinTable(name = "artwork_colors", joinColumns = @JoinColumn(name = "artwork_id"), inverseJoinColumns = @JoinColumn(name = "color_id"))
    private Set<Color> colors = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(name = "artwork_motifs", joinColumns = @JoinColumn(name = "artwork_id"), inverseJoinColumns = @JoinColumn(name = "motif_id"))
    private Set<Motif> motifs = new LinkedHashSet<>();

    @Convert(converter = ArtworkStatusConverter.class)
    @Column(name = "status", nullable = false, length = 20)
    private ArtworkStatus status = ArtworkStatus.DRAFT;

    @Column(name = "published_at")
    private Instant publishedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public Long getArtworkId() {
        return artworkId;
    }

    public void setArtworkId(Long artworkId) {
        this.artworkId = artworkId;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
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

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDimensionsLegacy() {
        return dimensionsLegacy;
    }

    public void setDimensionsLegacy(String dimensionsLegacy) {
        this.dimensionsLegacy = dimensionsLegacy;
    }

    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }

    public Style getStyle() {
        return style;
    }

    public void setStyle(Style style) {
        this.style = style;
    }

    public BigDecimal getWidthCm() {
        return widthCm;
    }

    public void setWidthCm(BigDecimal widthCm) {
        this.widthCm = widthCm;
    }

    public BigDecimal getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(BigDecimal heightCm) {
        this.heightCm = heightCm;
    }

    public BigDecimal getDepthCm() {
        return depthCm;
    }

    public void setDepthCm(BigDecimal depthCm) {
        this.depthCm = depthCm;
    }

    public BigDecimal getWeightG() {
        return weightG;
    }

    public void setWeightG(BigDecimal weightG) {
        this.weightG = weightG;
    }

    public SaleMode getSaleMode() {
        return saleMode;
    }

    public void setSaleMode(SaleMode saleMode) {
        this.saleMode = saleMode;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Set<Color> getColors() {
        return colors;
    }

    public void setColors(Set<Color> colors) {
        this.colors = colors;
    }

    public Set<Motif> getMotifs() {
        return motifs;
    }

    public void setMotifs(Set<Motif> motifs) {
        this.motifs = motifs;
    }

    public ArtworkStatus getStatus() {
        return status;
    }

    public void setStatus(ArtworkStatus status) {
        this.status = status;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }
}
