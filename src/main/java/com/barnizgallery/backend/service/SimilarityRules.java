package com.barnizgallery.backend.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.mapper.AiRequestMapper;
import com.barnizgallery.backend.mapper.ArtworkMapper;

/**
 * Fixed rules for "similar artworks", used when the AI service is off or fails: shared colors (35) and motifs
 * (25), same shape (20) and same style (20). The reason is written in the request language.
 */
final class SimilarityRules {

    private SimilarityRules() {
    }

    static double score(Artwork base, Artwork other) {
        double score = 35 * jaccard(ArtworkMapper.colorCodes(base), ArtworkMapper.colorCodes(other))
                + 25 * jaccard(ArtworkMapper.motifCodes(base), ArtworkMapper.motifCodes(other));
        if (sameCode(AiRequestMapper.shapeCode(base), AiRequestMapper.shapeCode(other))) {
            score += 20;
        }
        if (sameCode(AiRequestMapper.styleCode(base), AiRequestMapper.styleCode(other))) {
            score += 20;
        }
        return score;
    }

    static String reason(Artwork base, Artwork other, Locale locale) {
        boolean english = Locale.ENGLISH.getLanguage().equals(locale.getLanguage());
        List<String> parts = new ArrayList<>();
        if (sameCode(AiRequestMapper.shapeCode(base), AiRequestMapper.shapeCode(other))) {
            parts.add(english ? "shape" : "forma");
        }
        if (sameCode(AiRequestMapper.styleCode(base), AiRequestMapper.styleCode(other))) {
            parts.add(english ? "style" : "estilo");
        }
        List<String> colors = shared(ArtworkMapper.colorCodes(base), ArtworkMapper.colorCodes(other));
        if (!colors.isEmpty()) {
            parts.add((english ? "colors (" : "colores (") + String.join(", ", colors) + ")");
        }
        List<String> motifs = shared(ArtworkMapper.motifCodes(base), ArtworkMapper.motifCodes(other));
        if (!motifs.isEmpty()) {
            parts.add((english ? "motifs (" : "figuras (") + String.join(", ", motifs) + ")");
        }
        return (english ? "Similar in: " : "Se parece en: ") + String.join(", ", parts) + ".";
    }

    private static boolean sameCode(String a, String b) {
        return a != null && Objects.equals(a, b);
    }

    private static List<String> shared(List<String> a, List<String> b) {
        return a.stream().filter(b::contains).toList();
    }

    private static double jaccard(List<String> a, List<String> b) {
        Set<String> union = new HashSet<>(a);
        union.addAll(b);
        return union.isEmpty() ? 0 : (double) shared(a, b).size() / union.size();
    }
}
