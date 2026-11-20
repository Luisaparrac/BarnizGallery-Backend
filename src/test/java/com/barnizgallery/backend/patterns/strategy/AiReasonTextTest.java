package com.barnizgallery.backend.patterns.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.dto.AiRecommendationItem;
import com.barnizgallery.backend.dto.AiRecommendationReasons;
import com.barnizgallery.backend.enums.Language;

class AiReasonTextTest {

    private final AiRecommendationItem item = new AiRecommendationItem(5L, 2L, 0.9,
            new AiRecommendationReasons(List.of("rojo"), true, false, List.of(), null),
            " Coincide con lo que te gusta: rojo. ");

    @Test
    void spanishReadersGetTheExplanationOfTheService() {
        assertThat(AiReasonText.forItem(TestEntities.user(1, Language.ES), item))
                .isEqualTo("Coincide con lo que te gusta: rojo.");
    }

    @Test
    void englishReadersGetTheReasonsInEnglish() {
        assertThat(AiReasonText.forItem(TestEntities.user(1, Language.EN), item))
                .isEqualTo("Colors: rojo. Shapes and styles you like.");
    }
}
