package com.barnizgallery.backend.entity;

import java.time.LocalDateTime;

import com.barnizgallery.backend.converter.InteractionActionConverter;
import com.barnizgallery.backend.enums.InteractionAction;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * An interaction of a user with an artwork or with a store (exactly one of them).
 * Maps the table {@code museo.interactions}.
 */
@Entity
@Table(name = "interactions")
public class Interaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interaction_id")
    private Long interactionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artwork_id")
    private Artwork artwork;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @Convert(converter = InteractionActionConverter.class)
    @Column(name = "action", nullable = false, length = 30)
    private InteractionAction action;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "interaction_date")
    private LocalDateTime interactionDate;

    /** The column has no default, so the moment is set from Java when it is missing. */
    @PrePersist
    void onCreate() {
        if (interactionDate == null) {
            interactionDate = LocalDateTime.now();
        }
    }

    public Long getInteractionId() {
        return interactionId;
    }

    public void setInteractionId(Long interactionId) {
        this.interactionId = interactionId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Artwork getArtwork() {
        return artwork;
    }

    public void setArtwork(Artwork artwork) {
        this.artwork = artwork;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public InteractionAction getAction() {
        return action;
    }

    public void setAction(InteractionAction action) {
        this.action = action;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public LocalDateTime getInteractionDate() {
        return interactionDate;
    }

    public void setInteractionDate(LocalDateTime interactionDate) {
        this.interactionDate = interactionDate;
    }
}
