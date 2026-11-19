package com.barnizgallery.backend.entity;

import java.math.BigDecimal;
import java.time.Instant;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * An open, not expired custom request offered to artisans (read-only view {@code museo.v_open_custom_requests}).
 */
@Entity
@Immutable
@Table(name = "v_open_custom_requests")
public class OpenCustomRequestView {

    @Id
    @Column(name = "custom_request_id")
    private Long customRequestId;

    @Column(name = "buyer_id")
    private Long buyerId;

    @Column(name = "target_master_id")
    private Long targetMasterId;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "offered_price", precision = 14, scale = 2)
    private BigDecimal offeredPrice;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "desired_date")
    private Instant desiredDate;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "shape_code")
    private String shapeCode;

    @Column(name = "style_code")
    private String styleCode;

    public Long getCustomRequestId() {
        return customRequestId;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public Long getTargetMasterId() {
        return targetMasterId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getOfferedPrice() {
        return offeredPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public Instant getDesiredDate() {
        return desiredDate;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public String getShapeCode() {
        return shapeCode;
    }

    public String getStyleCode() {
        return styleCode;
    }
}
