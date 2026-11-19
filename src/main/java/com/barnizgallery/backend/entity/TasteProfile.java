package com.barnizgallery.backend.entity;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Taste profile of a user (1:1), filled from the initial questionnaire with catalog values.
 * v1 answers without a catalog match are kept by the database in {@code legacy_taste_profile_values}.
 * Maps the table {@code museo.taste_profiles}.
 */
@Entity
@Table(name = "taste_profiles")
public class TasteProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long profileId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_range_id")
    private BudgetRange budgetRange;

    @ManyToMany
    @JoinTable(name = "taste_profile_colors", joinColumns = @JoinColumn(name = "profile_id"), inverseJoinColumns = @JoinColumn(name = "color_id"))
    private Set<Color> colors = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(name = "taste_profile_shapes", joinColumns = @JoinColumn(name = "profile_id"), inverseJoinColumns = @JoinColumn(name = "shape_id"))
    private Set<Shape> shapes = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(name = "taste_profile_styles", joinColumns = @JoinColumn(name = "profile_id"), inverseJoinColumns = @JoinColumn(name = "style_id"))
    private Set<Style> styles = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(name = "taste_profile_motifs", joinColumns = @JoinColumn(name = "profile_id"), inverseJoinColumns = @JoinColumn(name = "motif_id"))
    private Set<Motif> motifs = new LinkedHashSet<>();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BudgetRange getBudgetRange() {
        return budgetRange;
    }

    public void setBudgetRange(BudgetRange budgetRange) {
        this.budgetRange = budgetRange;
    }

    public Set<Color> getColors() {
        return colors;
    }

    public void setColors(Set<Color> colors) {
        this.colors = colors;
    }

    public Set<Shape> getShapes() {
        return shapes;
    }

    public void setShapes(Set<Shape> shapes) {
        this.shapes = shapes;
    }

    public Set<Style> getStyles() {
        return styles;
    }

    public void setStyles(Set<Style> styles) {
        this.styles = styles;
    }

    public Set<Motif> getMotifs() {
        return motifs;
    }

    public void setMotifs(Set<Motif> motifs) {
        this.motifs = motifs;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
