package com.barnizgallery.backend.patterns.composite;

import com.barnizgallery.backend.dto.GalleryNodeResponse;

/**
 * <b>Composite pattern – Composite (root).</b>
 * <p>
 * The whole gallery. Its children are the rooms ({@link RoomComposite}).
 * Asking the root for {@link #countArtworks()} walks the whole tree.
 */
public class GalleryComposite extends CompositeGalleryComponent {

    private static final String NAME_ES = "Galería Barniz de Pasto Mopa-Mopa";
    private static final String NAME_EN = "Barniz de Pasto Mopa-Mopa Gallery";

    @Override
    public Integer getId() {
        return null;
    }

    @Override
    public String getType() {
        return "GALLERY";
    }

    @Override
    public String getNameEs() {
        return NAME_ES;
    }

    @Override
    public String getNameEn() {
        return NAME_EN;
    }

    @Override
    public GalleryNodeResponse toNode() {
        return new GalleryNodeResponse(null, getType(), NAME_ES, NAME_EN, countArtworks(),
                countAvailableForAuction(), null, null, null, null, null, null, childNodes());
    }
}
