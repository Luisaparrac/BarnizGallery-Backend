package com.barnizgallery.backend.patterns.composite;

import com.barnizgallery.backend.dto.response.GalleryNodeResponse;

/**
 * <b>Composite pattern – Component.</b>
 * <p>
 * Common interface for every element of the gallery tree: the whole gallery,
 * a room and an artwork. Thanks to it the client (the service and the frontend)
 * treats a single artwork and a group of artworks in the same way: for example,
 * {@link #countArtworks()} works for a leaf (returns 1) and for a composite
 * (adds the counts of its children).
 */
public interface GalleryComponent {

    /** Database id, or null for the gallery root. */
    Integer getId();

    /** GALLERY, ROOM or ARTWORK. */
    String getType();

    String getNameEs();

    String getNameEn();

    /** Number of artworks contained in this node (1 for an artwork). */
    int countArtworks();

    /** Number of artworks in this node that are currently in auction. */
    int countAvailableForAuction();

    /** Converts this node (and its children, recursively) to JSON-ready data. */
    GalleryNodeResponse toNode();
}
