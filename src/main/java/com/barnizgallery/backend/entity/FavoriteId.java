package com.barnizgallery.backend.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Composite key of {@link Favorite}: the user and the artwork.
 */
@Embeddable
public class FavoriteId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "artwork_id")
    private Long artworkId;

    public FavoriteId() {
    }

    public FavoriteId(Long userId, Long artworkId) {
        this.userId = userId;
        this.artworkId = artworkId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getArtworkId() {
        return artworkId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof FavoriteId that)) {
            return false;
        }
        return Objects.equals(userId, that.userId) && Objects.equals(artworkId, that.artworkId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, artworkId);
    }
}
