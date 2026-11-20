package com.barnizgallery.backend.mapper;

import com.barnizgallery.backend.dto.InteractionResponse;
import com.barnizgallery.backend.dto.RecommendationResponse;
import com.barnizgallery.backend.entity.Interaction;
import com.barnizgallery.backend.entity.Recommendation;
import com.barnizgallery.backend.entity.Store;

/**
 * Converts {@link Interaction} and {@link Recommendation} to their DTOs.
 */
public final class InteractionMapper {

    private InteractionMapper() {
    }

    public static InteractionResponse toResponse(Interaction interaction) {
        Long artworkId = interaction.getArtwork() == null ? null : interaction.getArtwork().getArtworkId();
        Long storeId = interaction.getStore() == null ? null : interaction.getStore().getStoreId();
        return new InteractionResponse(interaction.getInteractionId(), interaction.getUser().getUserId(), artworkId,
                storeId, interaction.getAction(), interaction.getDurationSeconds(), interaction.getInteractionDate());
    }

    public static RecommendationResponse toResponse(Recommendation recommendation) {
        Long artworkId = recommendation.getArtwork() == null ? null : recommendation.getArtwork().getArtworkId();
        Store store = recommendation.getStore();
        return new RecommendationResponse(recommendation.getRecommendationId(),
                recommendation.getUser().getUserId(), artworkId, store == null ? null : store.getStoreId(),
                store == null ? null : store.getNameEs(), store == null ? null : store.getNameEn(),
                recommendation.getScore(), recommendation.getReason(), recommendation.getStrategy(),
                recommendation.getRecommendationDate());
    }
}
