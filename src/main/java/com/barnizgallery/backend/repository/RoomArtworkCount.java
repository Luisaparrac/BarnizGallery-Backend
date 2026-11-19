package com.barnizgallery.backend.repository;

/**
 * Projection: number of artworks of one room ({@link ArtworkRepository#countGroupedByRoom()}).
 */
public interface RoomArtworkCount {

    Long getRoomId();

    long getArtworkCount();
}
