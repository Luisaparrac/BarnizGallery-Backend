package com.barnizgallery.backend.repository;

/**
 * Projection: number of interactions of all users with one artwork
 * ({@link InteractionRepository#countGroupedByArtwork()}).
 */
public interface ArtworkInteractionCount {

    Long getArtworkId();

    long getInteractionCount();
}
