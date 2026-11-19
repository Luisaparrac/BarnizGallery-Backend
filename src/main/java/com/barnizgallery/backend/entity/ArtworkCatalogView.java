package com.barnizgallery.backend.entity;

import java.math.BigDecimal;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.barnizgallery.backend.converter.ArtworkStatusConverter;
import com.barnizgallery.backend.converter.SaleModeConverter;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.SaleMode;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Public catalog row of an artwork (read-only view {@code museo.v_artwork_catalog}). The view only shows
 * exhibited or auctioned artworks, not deleted, of published stores of approved masters.
 * Photos and catalog codes come as JSON arrays.
 */
@Entity
@Immutable
@Table(name = "v_artwork_catalog")
public class ArtworkCatalogView {

    @Id
    @Column(name = "artwork_id")
    private Long artworkId;

    @Column(name = "slug")
    private String slug;

    @Column(name = "title_es", length = 150)
    private String titleEs;

    @Column(name = "title_en", length = 150)
    private String titleEn;

    @Column(name = "history_es")
    private String historyEs;

    @Column(name = "history_en")
    private String historyEn;

    @Column(name = "technique", length = 150)
    private String technique;

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

    @Convert(converter = ArtworkStatusConverter.class)
    @Column(name = "artwork_status", length = 20)
    private ArtworkStatus artworkStatus;

    @Column(name = "store_id")
    private Long storeId;

    @Column(name = "store_slug")
    private String storeSlug;

    @Column(name = "store_name_es", length = 150)
    private String storeNameEs;

    @Column(name = "store_name_en", length = 150)
    private String storeNameEn;

    @Column(name = "master_id")
    private Long masterId;

    @Column(name = "master_name", length = 100)
    private String masterName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "photos", columnDefinition = "jsonb")
    private String photos;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "color_codes", columnDefinition = "jsonb")
    private String colorCodes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "motif_codes", columnDefinition = "jsonb")
    private String motifCodes;

    public Long getArtworkId() {
        return artworkId;
    }

    public String getSlug() {
        return slug;
    }

    public String getTitleEs() {
        return titleEs;
    }

    public String getTitleEn() {
        return titleEn;
    }

    public String getHistoryEs() {
        return historyEs;
    }

    public String getHistoryEn() {
        return historyEn;
    }

    public String getTechnique() {
        return technique;
    }

    public BigDecimal getWidthCm() {
        return widthCm;
    }

    public BigDecimal getHeightCm() {
        return heightCm;
    }

    public BigDecimal getDepthCm() {
        return depthCm;
    }

    public BigDecimal getWeightG() {
        return weightG;
    }

    public SaleMode getSaleMode() {
        return saleMode;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public Integer getStock() {
        return stock;
    }

    public ArtworkStatus getArtworkStatus() {
        return artworkStatus;
    }

    public Long getStoreId() {
        return storeId;
    }

    public String getStoreSlug() {
        return storeSlug;
    }

    public String getStoreNameEs() {
        return storeNameEs;
    }

    public String getStoreNameEn() {
        return storeNameEn;
    }

    public Long getMasterId() {
        return masterId;
    }

    public String getMasterName() {
        return masterName;
    }

    public String getPhotos() {
        return photos;
    }

    public String getColorCodes() {
        return colorCodes;
    }

    public String getMotifCodes() {
        return motifCodes;
    }
}
