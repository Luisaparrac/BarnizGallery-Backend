package com.barnizgallery.backend.patterns.composite;

import com.barnizgallery.backend.dto.GalleryNodeResponse;

/**
 * <b>Composite pattern – Composite.</b>
 * <p>
 * A store of the gallery. Its children are the artworks ({@link ArtworkLeaf}) of the store.
 */
public class StoreComposite extends CompositeGalleryComponent {

    private final Long id;
    private final String nameEs;
    private final String nameEn;
    private final Long masterId;
    private final String masterName;

    public StoreComposite(Long id, String nameEs, String nameEn, Long masterId, String masterName) {
        this.id = id;
        this.nameEs = nameEs;
        this.nameEn = nameEn;
        this.masterId = masterId;
        this.masterName = masterName;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getType() {
        return "STORE";
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
    public GalleryNodeResponse toNode() {
        return new GalleryNodeResponse(id, getType(), nameEs, nameEn, countArtworks(), countAvailableForAuction(),
                null, null, masterId, masterName, childNodes());
    }
}
