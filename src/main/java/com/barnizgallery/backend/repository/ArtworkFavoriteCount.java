package com.barnizgallery.backend.repository;

/**
 * Projection: number of users who saved one artwork as favorite.
 */
public interface ArtworkFavoriteCount {

    Long getArtworkId();

    long getTotal();
}
