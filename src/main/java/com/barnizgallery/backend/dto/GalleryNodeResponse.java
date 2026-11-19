package com.barnizgallery.backend.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * One node of the gallery tree (GALLERY, STORE or ARTWORK). Fields that do not
 * apply to a node type are omitted from the JSON.
 *
 * @param id                       database id (null for the gallery root)
 * @param type                     GALLERY, STORE or ARTWORK
 * @param artworkCount             artworks under this node
 * @param availableForAuctionCount artworks under this node that are currently in auction
 * @param status                   artwork status (artworks only)
 * @param thumbnailUrl             first photo URL (artworks only, if any)
 * @param masterId                 master of the store (stores only)
 * @param masterName               master name (stores only)
 * @param children                 child nodes (gallery and stores)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record GalleryNodeResponse(
        Long id,
        String type,
        String nameEs,
        String nameEn,
        int artworkCount,
        int availableForAuctionCount,
        String status,
        String thumbnailUrl,
        Long masterId,
        String masterName,
        List<GalleryNodeResponse> children) {
}
