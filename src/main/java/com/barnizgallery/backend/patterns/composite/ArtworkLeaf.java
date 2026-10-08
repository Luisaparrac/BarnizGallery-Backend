package com.barnizgallery.backend.patterns.composite;

import com.barnizgallery.backend.dto.response.GalleryNodeResponse;
import com.barnizgallery.backend.model.enums.ArtworkStatus;

/**
 * <b>Composite pattern – Leaf.</b>
 * <p>
 * An artwork: the smallest element of the gallery. It has no children.
 * It carries what the 3D world needs to place it: status, URL of the GLB model
 * (if the model is completed) and a thumbnail (its first photo).
 */
public class ArtworkLeaf implements GalleryComponent {

    private final Integer id;
    private final String nameEs;
    private final String nameEn;
    private final ArtworkStatus status;
    private final String glbFileUrl;
    private final String thumbnailUrl;

    public ArtworkLeaf(Integer id, String nameEs, String nameEn, ArtworkStatus status, String glbFileUrl,
            String thumbnailUrl) {
        this.id = id;
        this.nameEs = nameEs;
        this.nameEn = nameEn;
        this.status = status;
        this.glbFileUrl = glbFileUrl;
        this.thumbnailUrl = thumbnailUrl;
    }

    @Override
    public Integer getId() {
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
                status.name(), glbFileUrl, thumbnailUrl, null, null, null, null);
    }
}
