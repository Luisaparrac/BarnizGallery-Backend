package com.barnizgallery.backend.mapper;

import java.util.List;

import com.barnizgallery.backend.dto.ArtworkDataResponse;
import com.barnizgallery.backend.dto.PhotoDataResponse;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Color;
import com.barnizgallery.backend.entity.Motif;
import com.barnizgallery.backend.entity.Photo;

/**
 * Builds the artwork as the frontend reads it.
 */
public final class ArtworkDataMapper {

    private ArtworkDataMapper() {
    }

    public static ArtworkDataResponse toData(Artwork a, List<Photo> photos) {
        return new ArtworkDataResponse(a.getArtworkId(), a.getStore().getStoreId(), a.getSlug(), a.getTitleEs(),
                a.getTitleEn(), a.getHistoryEs(), a.getHistoryEn(), a.getTechnique(), a.getWidthCm(), a.getHeightCm(),
                a.getDepthCm(), a.getWeightG(), a.getShape() == null ? null : a.getShape().getShapeId(),
                a.getStyle() == null ? null : a.getStyle().getStyleId(),
                a.getColors().stream().map(Color::getColorId).sorted().toList(),
                a.getMotifs().stream().map(Motif::getMotifId).sorted().toList(), a.getSaleMode(), a.getStatus(),
                a.getPrice(), a.getCurrency(), a.getStock(),
                photos.stream().map(ArtworkDataMapper::toData).toList(), a.getPublishedAt(), a.getCreatedAt());
    }

    public static PhotoDataResponse toData(Photo p) {
        return new PhotoDataResponse(p.getPhotoId(), p.getFileUrl(), p.getAltEs(), p.getAltEn(), p.getAngle(),
                p.getSortOrder(), p.isPrimary());
    }
}
