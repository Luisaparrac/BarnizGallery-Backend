package com.barnizgallery.backend.patterns.composite;

import com.barnizgallery.backend.dto.GalleryNodeResponse;
import com.barnizgallery.backend.enums.ArtworkStatus;

/**
 * <b>Composite pattern – Leaf.</b>
 * <p>
 * An artwork: the smallest element of the gallery. It has no children.
 * It carries its status and a thumbnail (its first photo).
 */
public class ArtworkLeaf implements GalleryComponent {

    private final Long id;
    private final String nameEs;
    private final String nameEn;
    private final ArtworkStatus status;
    private final String thumbnailUrl;

    public ArtworkLeaf(Long id, String nameEs, String nameEn, ArtworkStatus status,
            String thumbnailUrl) {
        this.id = id;
        this.nameEs = nameEs;
        this.nameEn = nameEn;
        this.status = status;
        this.thumbnailUrl = thumbnailUrl;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getType() {
        return "ARTWORK";
    }

    @Override
    public String getNameEs() {
        return nameEs;
    }

    @Override
    public String getNameEn() {
        return nameEn;
    }

    @Override
    public int countArtworks() {
        return 1;
    }

    @Override
    public int countAvailableForAuction() {
        return status == ArtworkStatus.IN_AUCTION ? 1 : 0;
    }

    @Override
    public GalleryNodeResponse toNode() {
        return new GalleryNodeResponse(id, getType(), nameEs, nameEn, countArtworks(), countAvailableForAuction(),
                status.name(), thumbnailUrl, null, null, null);
    }
}
