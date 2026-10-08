package com.barnizgallery.backend.model.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
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
 * Taste profile of a visitor (1:1), filled from the initial questionnaire. Maps the existing table {@code taste_profiles}.
 */
@Entity
@Table(name = "taste_profiles")
public class TasteProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Integer profileId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "visitor_id", nullable = false, unique = true)
    private Visitor visitor;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "preferred_colors", columnDefinition = "text[]")
    private List<String> preferredColors;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "preferred_types", columnDefinition = "text[]")
    private List<String> preferredTypes;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "preferred_styles", columnDefinition = "text[]")
    private List<String> preferredStyles;

    @Column(name = "budget_range", length = 100)
    private String budgetRange;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** The column has a DB default, but the value is set from Java so we do not depend on it. */
    @PrePersist
    void onCreate() {
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    public Integer getProfileId() {
        return profileId;
    }

    public void setProfileId(Integer profileId) {
        this.profileId = profileId;
    }

    public Visitor getVisitor() {
        return visitor;
    }

    public void setVisitor(Visitor visitor) {
        this.visitor = visitor;
    }

    public List<String> getPreferredColors() {
        return preferredColors;
    }

    public void setPreferredColors(List<String> preferredColors) {
        this.preferredColors = preferredColors;
    }

    public List<String> getPreferredTypes() {
        return preferredTypes;
    }

    public void setPreferredTypes(List<String> preferredTypes) {
        this.preferredTypes = preferredTypes;
    }

    public List<String> getPreferredStyles() {
        return preferredStyles;
    }

    public void setPreferredStyles(List<String> preferredStyles) {
        this.preferredStyles = preferredStyles;
    }

    public String getBudgetRange() {
        return budgetRange;
    }

    public void setBudgetRange(String budgetRange) {
        this.budgetRange = budgetRange;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
