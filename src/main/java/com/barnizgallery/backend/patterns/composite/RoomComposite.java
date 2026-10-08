package com.barnizgallery.backend.patterns.composite;

import com.barnizgallery.backend.dto.response.GalleryNodeResponse;

/**
 * <b>Composite pattern – Composite.</b>
 * <p>
 * A room of the gallery. Its children are the artworks ({@link ArtworkLeaf}) of the room.
 */
public class RoomComposite extends CompositeGalleryComponent {

    private final Integer id;
    private final String nameEs;
    private final String nameEn;
    private final String scene3dUrl;
    private final Integer masterId;
    private final String masterName;

    public RoomComposite(Integer id, String nameEs, String nameEn, String scene3dUrl, Integer masterId,
            String masterName) {
        this.id = id;
        this.nameEs = nameEs;
        this.nameEn = nameEn;
        this.scene3dUrl = scene3dUrl;
        this.masterId = masterId;
        this.masterName = masterName;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public String getType() {
        return "ROOM";
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
                null, null, null, scene3dUrl, masterId, masterName, childNodes());
    }
}
