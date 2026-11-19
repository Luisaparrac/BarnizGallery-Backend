package com.barnizgallery.backend.mapper;

import java.util.List;

import com.barnizgallery.backend.dto.ArtworkDetailResponse;
import com.barnizgallery.backend.dto.ArtworkRequest;
import com.barnizgallery.backend.dto.ArtworkSummaryResponse;
import com.barnizgallery.backend.dto.AuctionResponse;
import com.barnizgallery.backend.dto.PhotoResponse;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Color;
import com.barnizgallery.backend.entity.Motif;
import com.barnizgallery.backend.entity.Photo;

/**
 * Converts between {@link Artwork} (and {@link Photo}) and their DTOs.
 */
public final class ArtworkMapper {

    private ArtworkMapper() {
    }

    public static ArtworkSummaryResponse toSummary(Artwork artwork) {
        return new ArtworkSummaryResponse(artwork.getArtworkId(), artwork.getStore().getStoreId(), artwork.getTitleEs(),
                artwork.getTitleEn(), artwork.getTechnique(), colorCodes(artwork), motifCodes(artwork),
                artwork.getStatus(), artwork.getSaleMode(), artwork.getPrice(), artwork.getCurrency(),
                artwork.getStock());
    }

    public static ArtworkDetailResponse toDetail(Artwork artwork, List<PhotoResponse> photos,
            AuctionResponse currentAuction) {
        return new ArtworkDetailResponse(artwork.getArtworkId(), artwork.getStore().getStoreId(), artwork.getTitleEs(),
                artwork.getTitleEn(), artwork.getHistoryEs(), artwork.getHistoryEn(), artwork.getTechnique(),
                artwork.getDimensionsLegacy(), artwork.getWidthCm(), artwork.getHeightCm(), artwork.getDepthCm(),
                artwork.getWeightG(), colorCodes(artwork), motifCodes(artwork), artwork.getStatus(),
                artwork.getSaleMode(), artwork.getPrice(), artwork.getCurrency(), artwork.getStock(), photos,
                currentAuction);
    }

    /**
     * Copies the request fields into the entity. The store, status and catalog relations are set by the
     * service.
     */
    public static void apply(ArtworkRequest request, Artwork artwork) {
        artwork.setTitleEs(request.titleEs());
        artwork.setTitleEn(request.titleEn());
        artwork.setHistoryEs(request.historyEs());
        artwork.setHistoryEn(request.historyEn());
        artwork.setTechnique(request.technique());
        artwork.setWidthCm(request.widthCm());
        artwork.setHeightCm(request.heightCm());
        artwork.setDepthCm(request.depthCm());
        artwork.setWeightG(request.weightG());
        artwork.setSaleMode(request.saleMode());
        artwork.setPrice(request.price());
        artwork.setCurrency(request.currency());
        artwork.setStock(request.stock());
    }

    /** Color catalog codes of the artwork, sorted. */
    public static List<String> colorCodes(Artwork artwork) {
        return artwork.getColors().stream().map(Color::getCode).sorted().toList();
    }

    /** Motif catalog codes of the artwork, sorted. */
    public static List<String> motifCodes(Artwork artwork) {
        return artwork.getMotifs().stream().map(Motif::getCode).sorted().toList();
    }

    public static PhotoResponse toPhotoResponse(Photo photo) {
        return new PhotoResponse(photo.getPhotoId(), photo.getArtwork().getArtworkId(), photo.getFileUrl(),
                photo.getAngle());
    }
}
