package com.barnizgallery.backend.patterns.strategy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import com.barnizgallery.backend.dto.AiRecommendationReasons;
import com.barnizgallery.backend.entity.User;

/**
 * Writes the reason of a store recommended by the AI service from the reasons of its artworks, in the user's
 * language. Example: "Colores: rojo, verde. Figuras: colibri. Formas y estilos que te gustan."
 */
final class AiReasonText {

    static final int MAX_CODES = 3;

    private AiReasonText() {
    }

    static String describe(User user, List<AiRecommendationReasons> reasons) {
        List<String> parts = new ArrayList<>();
        List<String> colors = mostFrequent(reasons, AiRecommendationReasons::colors);
        List<String> motifs = mostFrequent(reasons, AiRecommendationReasons::motifs);
        if (!colors.isEmpty()) {
            String codes = String.join(", ", colors);
            parts.add(LocalizedText.pick(user, "Colores: " + codes + ".", "Colors: " + codes + "."));
        }
        if (!motifs.isEmpty()) {
            String codes = String.join(", ", motifs);
            parts.add(LocalizedText.pick(user, "Figuras: " + codes + ".", "Motifs: " + codes + "."));
        }
        if (reasons.stream().anyMatch(r -> r.shape() || r.style())) {
            parts.add(LocalizedText.pick(user, "Formas y estilos que te gustan.", "Shapes and styles you like."));
        }
        if (reasons.stream().anyMatch(r -> Boolean.TRUE.equals(r.budget()))) {
            parts.add(LocalizedText.pick(user, "Obras dentro de tu presupuesto.", "Artworks within your budget."));
        }
        if (parts.isEmpty()) {
            return LocalizedText.pick(user, "Obras populares en la galería.", "Popular artworks in the gallery.");
        }
        return String.join(" ", parts);
    }

    /** The {@value #MAX_CODES} most repeated codes; ties keep the order in which they first appear. */
    private static List<String> mostFrequent(List<AiRecommendationReasons> reasons,
            Function<AiRecommendationReasons, List<String>> codes) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        reasons.stream()
                .map(codes)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .forEach(code -> counts.merge(code, 1, Integer::sum));
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(MAX_CODES)
                .map(Map.Entry::getKey)
                .toList();
    }
}
