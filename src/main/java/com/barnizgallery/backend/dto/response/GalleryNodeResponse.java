package com.barnizgallery.backend.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * One node of the gallery tree (GALLERY, ROOM or ARTWORK). Fields that do not
 * apply to a node type are omitted from the JSON.
 *
 * @param id                       database id (null for the gallery root)
 * @param type                     GALLERY, ROOM or ARTWORK
 * @param artworkCount             artworks under this node
 * @param availableForAuctionCount artworks under this node that are currently in auction
 * @param status                   artwork status (artworks only)
 * @param glbFileUrl               completed 3D model URL (artworks only, if any)
 * @param thumbnailUrl             first photo URL (artworks only, if any)
 * @param scene3dUrl               3D scene URL (rooms only)
 * @param masterId                 master of the room (rooms only)
 * @param masterName               master name (rooms only)
 * @param children                 child nodes (gallery and rooms)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record GalleryNodeResponse(
        Integer id,
        String type,
        String nameEs,
        String nameEn,
        int artworkCount,
        int availableForAuctionCount,
        String status,
        String glbFileUrl,
        String thumbnailUrl,
        String scene3dUrl,
        Integer masterId,
        String masterName,
        List<GalleryNodeResponse> children) {
}
