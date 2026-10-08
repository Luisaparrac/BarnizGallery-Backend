package com.barnizgallery.backend.mapper;

import com.barnizgallery.backend.dto.InteractionResponse;
import com.barnizgallery.backend.dto.RecommendationResponse;
import com.barnizgallery.backend.entity.Interaction;
import com.barnizgallery.backend.entity.Recommendation;

/**
 * Converts {@link Interaction} and {@link Recommendation} to their DTOs.
 */
public final class InteractionMapper {

    private InteractionMapper() {
    }

    public static InteractionResponse toResponse(Interaction interaction) {
        return new InteractionResponse(interaction.getInteractionId(), interaction.getVisitor().getVisitorId(),
                interaction.getArtwork().getArtworkId(), interaction.getAction(), interaction.getDurationSeconds(),
                interaction.getInteractionDate());
    }

    public static RecommendationResponse toResponse(Recommendation recommendation) {
        return new RecommendationResponse(recommendation.getRecommendationId(),
                recommendation.getVisitor().getVisitorId(), recommendation.getRoom().getRoomId(),
                recommendation.getRoom().getNameEs(), recommendation.getRoom().getNameEn(),
                recommendation.getScore(), recommendation.getReason(), recommendation.getRecommendationDate());
    }
}
