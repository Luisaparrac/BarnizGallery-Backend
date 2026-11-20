package com.barnizgallery.backend.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.barnizgallery.backend.dto.AiBudget;
import com.barnizgallery.backend.dto.AiCandidate;
import com.barnizgallery.backend.dto.AiComparable;
import com.barnizgallery.backend.dto.AiInteraction;
import com.barnizgallery.backend.dto.AiPricingPiece;
import com.barnizgallery.backend.dto.AiPricingRequest;
import com.barnizgallery.backend.dto.AiTasteProfile;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.BudgetRange;
import com.barnizgallery.backend.entity.Interaction;
import com.barnizgallery.backend.entity.Motif;
import com.barnizgallery.backend.entity.Shape;
import com.barnizgallery.backend.entity.Style;
import com.barnizgallery.backend.entity.TasteProfile;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.InteractionAction;
import com.barnizgallery.backend.service.SlugGenerator;

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

    /** An available artwork the service can rank; {@code popularity} is its number of interactions. */
    public static AiCandidate toCandidate(Artwork artwork, long popularity) {
        return new AiCandidate(artwork.getArtworkId(), slugOf(artwork), artwork.getStore().getStoreId(),
                ArtworkMapper.colorCodes(artwork), shapeCode(artwork), styleCode(artwork),
                ArtworkMapper.motifCodes(artwork), artwork.getPrice(), artwork.getCurrency(), popularity);
    }

    /** Taste profile as catalog codes; the budget is only sent when its range has a currency. */
    public static AiTasteProfile toProfile(TasteProfile profile) {
        BudgetRange range = profile.getBudgetRange();
        AiBudget budget = range == null || range.getCurrency() == null ? null
                : new AiBudget(range.getMinAmount(), range.getMaxAmount(), range.getCurrency());
        return new AiTasteProfile(UserMapper.colorCodes(profile),
                profile.getShapes().stream().map(Shape::getCode).sorted().toList(),
                profile.getStyles().stream().map(Style::getCode).sorted().toList(),
                profile.getMotifs().stream().map(Motif::getCode).sorted().toList(), budget);
    }

    /** Interaction with an artwork; actions the service does not know (bids) are left out. */
    public static Optional<AiInteraction> toInteraction(Interaction interaction) {
        if (interaction.getArtwork() == null || interaction.getAction() == InteractionAction.BID) {
            return Optional.empty();
        }
        return Optional.of(new AiInteraction(interaction.getArtwork().getArtworkId(),
                interaction.getAction().name(), interaction.getDurationSeconds()));
    }

    private static AiComparable toComparable(Artwork artwork) {
        return new AiComparable(artwork.getArtworkId(), shapeCode(artwork), styleCode(artwork),
                ArtworkMapper.colorCodes(artwork), ArtworkMapper.motifCodes(artwork), measure(artwork.getWidthCm()),
                measure(artwork.getHeightCm()), measure(artwork.getDepthCm()), artwork.getPrice(),
                artwork.getCurrency(), artwork.getStatus() == ArtworkStatus.SOLD);
    }

    /**
     * The artwork's slug; an artwork without one gets a slug made from its Spanish title, only for the request
     * (it is not saved).
     */
    static String slugOf(Artwork artwork) {
        return artwork.getSlug() != null ? artwork.getSlug() : SlugGenerator.slugOf(artwork.getTitleEs());
    }

    /** Code of the artwork's shape, or null. */
    public static String shapeCode(Artwork artwork) {
        return artwork.getShape() == null ? null : artwork.getShape().getCode();
    }

    /** Code of the artwork's style, or null. */
    public static String styleCode(Artwork artwork) {
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
