package com.barnizgallery.backend.patterns.strategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.TasteProfile;
import com.barnizgallery.backend.entity.Visitor;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.TasteProfileRepository;

/**
 * <b>Strategy pattern – ConcreteStrategy "profile".</b>
 * <p>
 * Compares the colors of the visitor's taste profile with the {@code color_tags} of the
 * artworks of each room. Score = 100 × (preferred colors found in the room) / (preferred colors).
 */
@Component
public class ProfileBasedStrategy implements RecommendationStrategy {

    private final TasteProfileRepository tasteProfileRepository;
    private final ArtworkRepository artworkRepository;

    public ProfileBasedStrategy(TasteProfileRepository tasteProfileRepository,
            ArtworkRepository artworkRepository) {
        this.tasteProfileRepository = tasteProfileRepository;
        this.artworkRepository = artworkRepository;
    }

    @Override
    public String name() {
        return "profile";
    }

    @Override
    public List<RecommendationResult> recommend(Visitor visitor) {
        // TODO: use piece_type/style when the columns exist (preferred_types / preferred_styles)
        Optional<TasteProfile> profile = tasteProfileRepository.findByVisitorVisitorId(visitor.getVisitorId());
        Set<String> preferred = normalize(profile.map(TasteProfile::getPreferredColors).orElse(null));
        if (preferred.isEmpty()) {
            return List.of();
        }

        Map<Integer, Set<String>> tagsByRoom = new LinkedHashMap<>();
        for (Artwork artwork : artworkRepository.findAllByOrderByArtworkIdAsc()) {
            tagsByRoom.computeIfAbsent(artwork.getRoom().getRoomId(), id -> new LinkedHashSet<>())
                    .addAll(normalize(artwork.getColorTags()));
        }

        List<RecommendationResult> results = new ArrayList<>();
        tagsByRoom.forEach((roomId, roomTags) -> {
            List<String> matched = preferred.stream().filter(roomTags::contains).toList();
            if (!matched.isEmpty()) {
                double score = 100.0 * matched.size() / preferred.size();
                String colors = String.join(", ", matched);
                String reason = LocalizedText.pick(visitor,
                        "Coincide con tus colores preferidos: " + colors,
                        "Matches your preferred colors: " + colors);
                results.add(RecommendationResult.of(roomId, score, reason));
            }
        });
        results.sort(Comparator.comparing(RecommendationResult::score).reversed());
        return results;
    }

    private static Set<String> normalize(List<String> values) {
        Set<String> normalized = new LinkedHashSet<>();
        if (values != null) {
            for (String value : values) {
                if (value != null && !value.isBlank()) {
                    normalized.add(value.trim().toLowerCase(Locale.ROOT));
                }
            }
        }
        return normalized;
    }
}
