package com.barnizgallery.backend.mapper;

import java.util.List;

import com.barnizgallery.backend.dto.ArtworkDetailResponse;
import com.barnizgallery.backend.dto.ArtworkRequest;
import com.barnizgallery.backend.dto.ArtworkSummaryResponse;
import com.barnizgallery.backend.dto.AuctionResponse;
import com.barnizgallery.backend.dto.PhotoResponse;
import com.barnizgallery.backend.dto.ThreeDModelResponse;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Photo;

/**
 * Converts between {@link Artwork} (and {@link Photo}) and their DTOs.
 */
public final class ArtworkMapper {

    private ArtworkMapper() {
    }

    public static ArtworkSummaryResponse toSummary(Artwork artwork) {
        return new ArtworkSummaryResponse(artwork.getArtworkId(), artwork.getRoom().getRoomId(), artwork.getTitleEs(),
                artwork.getTitleEn(), artwork.getTechnique(), artwork.getDimensions(), artwork.getColorTags(),
                artwork.getMotifTags(), artwork.getStatus());
    }

    public static ArtworkDetailResponse toDetail(Artwork artwork, List<PhotoResponse> photos,
            ThreeDModelResponse model, AuctionResponse currentAuction) {
        return new ArtworkDetailResponse(artwork.getArtworkId(), artwork.getRoom().getRoomId(), artwork.getTitleEs(),
                artwork.getTitleEn(), artwork.getHistoryEs(), artwork.getHistoryEn(), artwork.getTechnique(),
                artwork.getDimensions(), artwork.getColorTags(), artwork.getMotifTags(), artwork.getStatus(),
                photos, model, currentAuction);
    }

    /** Copies the request fields into the entity. The room and status are set by the service. */
    public static void apply(ArtworkRequest request, Artwork artwork) {
        artwork.setTitleEs(request.titleEs());
        artwork.setTitleEn(request.titleEn());
        artwork.setHistoryEs(request.historyEs());
        artwork.setHistoryEn(request.historyEn());
        artwork.setTechnique(request.technique());
        artwork.setDimensions(request.dimensions());
        artwork.setColorTags(request.colorTags());
        artwork.setMotifTags(request.motifTags());
    }

    public static PhotoResponse toPhotoResponse(Photo photo) {
        return new PhotoResponse(photo.getPhotoId(), photo.getArtwork().getArtworkId(), photo.getFileUrl(),
                photo.getAngle());
    }
}
