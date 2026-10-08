package com.barnizgallery.backend.patterns.strategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.barnizgallery.backend.exception.FeatureDisabledException;
import com.barnizgallery.backend.model.entity.Artwork;
import com.barnizgallery.backend.model.entity.Room;
import com.barnizgallery.backend.model.entity.TasteProfile;
import com.barnizgallery.backend.model.entity.Visitor;
import com.barnizgallery.backend.model.enums.Language;
import com.barnizgallery.backend.patterns.adapter.AiTextClient;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.RoomRepository;
import com.barnizgallery.backend.repository.TasteProfileRepository;

/**
 * <b>Strategy pattern – ConcreteStrategy "ai".</b>
 * <p>
 * Asks the AI model (through the {@link AiTextClient} adapter) to rank the rooms.
 * Only available when an AI provider is configured. Answers that cannot be parsed
 * or that mention unknown rooms are ignored: nothing is invented.
 */
@Component
public class AiStrategy implements RecommendationStrategy {

    private static final Logger log = LoggerFactory.getLogger(AiStrategy.class);

    static final String SYSTEM_PROMPT = """
            You recommend rooms of a virtual museum of Barniz de Pasto Mopa-Mopa.
            Answer ONLY with lines in the format roomId|score|reason
            where score is a number from 0 to 100 and reason is one short sentence
            in the requested language. Use only the room ids you receive.""";

    private final AiTextClient aiClient;
    private final RoomRepository roomRepository;
    private final ArtworkRepository artworkRepository;
    private final TasteProfileRepository tasteProfileRepository;

    public AiStrategy(AiTextClient aiClient, RoomRepository roomRepository, ArtworkRepository artworkRepository,
            TasteProfileRepository tasteProfileRepository) {
        this.aiClient = aiClient;
        this.roomRepository = roomRepository;
        this.artworkRepository = artworkRepository;
        this.tasteProfileRepository = tasteProfileRepository;
    }

    @Override
    public String name() {
        return "ai";
    }

    @Override
    public List<RecommendationResult> recommend(Visitor visitor) {
        if (!aiClient.isEnabled()) {
            throw new FeatureDisabledException("The 'ai' recommendation strategy needs an AI provider");
        }
        String answer = aiClient.complete(SYSTEM_PROMPT, buildUserPrompt(visitor));
        Set<Integer> validRooms = new LinkedHashSet<>();
        roomRepository.findAll().forEach(room -> validRooms.add(room.getRoomId()));
        List<RecommendationResult> results = parse(answer, validRooms);
        results.sort(Comparator.comparing(RecommendationResult::score).reversed());
        return results;
    }

    private String buildUserPrompt(Visitor visitor) {
        Map<Integer, Set<String>> tagsByRoom = new LinkedHashMap<>();
        for (Artwork artwork : artworkRepository.findAllByOrderByArtworkIdAsc()) {
            Set<String> tags = tagsByRoom.computeIfAbsent(artwork.getRoom().getRoomId(), id -> new LinkedHashSet<>());
            if (artwork.getColorTags() != null) {
                tags.addAll(artwork.getColorTags());
            }
            if (artwork.getMotifTags() != null) {
                tags.addAll(artwork.getMotifTags());
            }
        }
        StringBuilder prompt = new StringBuilder();
        prompt.append("Language: ").append(visitor.getPreferredLanguage() == Language.EN ? "English" : "Spanish")
                .append('\n');
        tasteProfileRepository.findByVisitorVisitorId(visitor.getVisitorId()).ifPresent((TasteProfile p) -> prompt
                .append("Visitor preferences: colors=").append(p.getPreferredColors())
                .append(", types=").append(p.getPreferredTypes())
                .append(", styles=").append(p.getPreferredStyles())
                .append(", budget=").append(p.getBudgetRange()).append('\n'));
        prompt.append("Rooms:\n");
        for (Room room : roomRepository.findAllWithMaster()) {
            prompt.append(room.getRoomId()).append(" | ").append(room.getNameEn())
                    .append(" | tags=").append(tagsByRoom.getOrDefault(room.getRoomId(), Set.of())).append('\n');
        }
        return prompt.toString();
    }

    /** Parses lines "roomId|score|reason". Invalid lines are skipped. */
    static List<RecommendationResult> parse(String answer, Set<Integer> validRooms) {
        List<RecommendationResult> results = new ArrayList<>();
        if (answer == null) {
            return results;
        }
        Set<Integer> seen = new LinkedHashSet<>();
        for (String line : answer.split("\\R")) {
            String[] parts = line.split("\\|", 3);
            if (parts.length < 3) {
                continue;
            }
            try {
                Integer roomId = Integer.valueOf(parts[0].trim());
                double score = Double.parseDouble(parts[1].trim());
                if (validRooms.contains(roomId) && seen.add(roomId)) {
                    results.add(RecommendationResult.of(roomId, score, parts[2].trim()));
                }
            } catch (NumberFormatException ex) {
                log.debug("Ignoring AI line that cannot be parsed: {}", line);
            }
        }
        return results;
    }
}
