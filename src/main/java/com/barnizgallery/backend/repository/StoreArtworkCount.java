package com.barnizgallery.backend.repository;

/**
 * Projection: number of artworks of one store ({@link ArtworkRepository#countGroupedByStore()}).
 */
public interface StoreArtworkCount {

    Long getStoreId();

    long getArtworkCount();
}
