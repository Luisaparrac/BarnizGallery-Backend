package com.barnizgallery.backend.repository;

/**
 * Projection: number of artworks of one store ({@link ArtworkRepository#countGroupedByStore(java.util.Collection)}).
 */
public interface StoreArtworkCount {

    Long getStoreId();

    long getArtworkCount();
}
