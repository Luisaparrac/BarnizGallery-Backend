package com.barnizgallery.backend.mapper;

import java.math.BigDecimal;
import java.util.List;

import com.barnizgallery.backend.dto.AiComparable;
import com.barnizgallery.backend.dto.AiPricingPiece;
import com.barnizgallery.backend.dto.AiPricingRequest;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.enums.ArtworkStatus;

/**
 * Builds the requests of the AI service from the entities, with catalog codes. Values the service would reject
 * (codes longer than {@value #MAX_CODE_LENGTH} characters, measures outside 0-{@value #MAX_MEASURE_CM} cm) are
 * sent as null instead.
 */
public final class AiRequestMapper {

    static final int MAX_CODE_LENGTH = 60;
    static final int MAX_MEASURE_CM = 1000;

    private AiRequestMapper() {
    }

    /** Price request for {@code piece} in {@code currency}, compared with priced artworks. */
    public static AiPricingRequest toPricingRequest(Artwork piece, List<Artwork> comparables, String currency) {
        AiPricingPiece description = new AiPricingPiece(shapeCode(piece), styleCode(piece),
                ArtworkMapper.colorCodes(piece), ArtworkMapper.motifCodes(piece), measure(piece.getWidthCm()),
                measure(piece.getHeightCm()), measure(piece.getDepthCm()), code(piece.getTechnique()));
        return new AiPricingRequest(description, comparables.stream().map(AiRequestMapper::toComparable).toList(),
                currency);
    }

    private static AiComparable toComparable(Artwork artwork) {
        return new AiComparable(artwork.getArtworkId(), shapeCode(artwork), styleCode(artwork),
                ArtworkMapper.colorCodes(artwork), ArtworkMapper.motifCodes(artwork), measure(artwork.getWidthCm()),
                measure(artwork.getHeightCm()), measure(artwork.getDepthCm()), artwork.getPrice(),
                artwork.getCurrency(), artwork.getStatus() == ArtworkStatus.SOLD);
    }

    private static String shapeCode(Artwork artwork) {
        return artwork.getShape() == null ? null : artwork.getShape().getCode();
    }

    private static String styleCode(Artwork artwork) {
        return artwork.getStyle() == null ? null : artwork.getStyle().getCode();
    }

    private static String code(String value) {
        if (value == null || value.isBlank() || value.strip().length() > MAX_CODE_LENGTH) {
            return null;
        }
        return value.strip();
    }

    private static BigDecimal measure(BigDecimal value) {
        if (value == null || value.signum() <= 0 || value.compareTo(BigDecimal.valueOf(MAX_MEASURE_CM)) > 0) {
            return null;
        }
        return value;
    }
}
