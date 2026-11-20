package com.barnizgallery.backend.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.barnizgallery.backend.converter.CustomRequestStatusConverter;
import com.barnizgallery.backend.enums.CustomRequestStatus;

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
 * A custom order requested by a buyer, open to offers from artisans. Its status is kept as text because
 * the database does not define the list of statuses yet. Maps the table {@code museo.custom_requests}.
 */
@Entity
@Table(name = "custom_requests")
public class CustomRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "custom_request_id")
    private Long customRequestId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_master_id")
    private Master targetMaster;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shape_id", nullable = false)
    private Shape shape;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "style_id", nullable = false)
    private Style style;

    @Column(name = "size_notes")
    private String sizeNotes;

    @Column(name = "offered_price", nullable = false, precision = 14, scale = 2)
    private BigDecimal offeredPrice;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "desired_date")
    private Instant desiredDate;

    @Convert(converter = CustomRequestStatusConverter.class)
    @Column(name = "status", nullable = false, length = 30)
    private CustomRequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accepted_offer_id")
    private CustomOffer acceptedOffer;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @ManyToMany
    @JoinTable(name = "custom_request_colors", joinColumns = @JoinColumn(name = "custom_request_id"), inverseJoinColumns = @JoinColumn(name = "color_id"))
    private Set<Color> colors = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(name = "custom_request_motifs", joinColumns = @JoinColumn(name = "custom_request_id"), inverseJoinColumns = @JoinColumn(name = "motif_id"))
    private Set<Motif> motifs = new LinkedHashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Long getCustomRequestId() {
        return customRequestId;
    }

    public void setCustomRequestId(Long customRequestId) {
        this.customRequestId = customRequestId;
    }

    public User getBuyer() {
        return buyer;
    }

    public void setBuyer(User buyer) {
        this.buyer = buyer;
    }

    public Master getTargetMaster() {
        return targetMaster;
    }

    public void setTargetMaster(Master targetMaster) {
        this.targetMaster = targetMaster;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getSizeNotes() {
        return sizeNotes;
    }

    public void setSizeNotes(String sizeNotes) {
        this.sizeNotes = sizeNotes;
    }

    public BigDecimal getOfferedPrice() {
        return offeredPrice;
    }

    public void setOfferedPrice(BigDecimal offeredPrice) {
        this.offeredPrice = offeredPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Instant getDesiredDate() {
        return desiredDate;
    }

    public void setDesiredDate(Instant desiredDate) {
        this.desiredDate = desiredDate;
    }

    public CustomRequestStatus getStatus() {
        return status;
    }

    public void setStatus(CustomRequestStatus status) {
        this.status = status;
    }

    public CustomOffer getAcceptedOffer() {
        return acceptedOffer;
    }

    public void setAcceptedOffer(CustomOffer acceptedOffer) {
        this.acceptedOffer = acceptedOffer;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
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
}
