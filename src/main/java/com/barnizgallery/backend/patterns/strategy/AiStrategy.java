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

import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Store;
import com.barnizgallery.backend.entity.TasteProfile;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.exception.FeatureDisabledException;
import com.barnizgallery.backend.mapper.ArtworkMapper;
import com.barnizgallery.backend.mapper.UserMapper;
import com.barnizgallery.backend.patterns.adapter.AiTextClient;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.StoreRepository;
import com.barnizgallery.backend.repository.TasteProfileRepository;

/**
 * <b>Strategy pattern – ConcreteStrategy "ai".</b>
 * <p>
 * Asks the AI model (through the {@link AiTextClient} adapter) to rank the stores.
 * Only available when an AI provider is configured. Answers that cannot be parsed
 * or that mention unknown stores are ignored: nothing is invented.
 */
@Component
public class AiStrategy implements RecommendationStrategy {

    private static final Logger log = LoggerFactory.getLogger(AiStrategy.class);

    static final String SYSTEM_PROMPT = """
            You recommend stores of a virtual museum of Barniz de Pasto Mopa-Mopa.
            Answer ONLY with lines in the format storeId|score|reason
            where score is a number from 0 to 100 and reason is one short sentence
            in the requested language. Use only the store ids you receive.""";

    private final AiTextClient aiClient;
    private final StoreRepository storeRepository;
    private final ArtworkRepository artworkRepository;
    private final TasteProfileRepository tasteProfileRepository;
    private final HybridStrategy hybridStrategy;

    public AiStrategy(AiTextClient aiClient, StoreRepository storeRepository, ArtworkRepository artworkRepository,
            TasteProfileRepository tasteProfileRepository, HybridStrategy hybridStrategy) {
        this.aiClient = aiClient;
        this.storeRepository = storeRepository;
        this.artworkRepository = artworkRepository;
        this.tasteProfileRepository = tasteProfileRepository;
        this.hybridStrategy = hybridStrategy;
    }

    @Override
    public String name() {
        return "ai";
    }

    @Override
    public List<RecommendationResult> recommend(User user) {
        if (!aiClient.isEnabled()) {
            throw new FeatureDisabledException(ErrorCode.AI_STRATEGY_DISABLED);
        }
        String answer = aiClient.complete(SYSTEM_PROMPT, buildUserPrompt(user));
        Set<Long> validStores = new LinkedHashSet<>();
        storeRepository.findAll().forEach(store -> validStores.add(store.getStoreId()));
        List<RecommendationResult> results = parse(answer, validStores);
        results.sort(Comparator.comparing(RecommendationResult::score).reversed());
        return results;
    }

    /** The text model only ranks stores; artworks are recommended by the hybrid strategy. */
    @Override
    public List<RecommendationResult> recommendArtworks(User user) {
        if (!aiClient.isEnabled()) {
            throw new FeatureDisabledException(ErrorCode.AI_STRATEGY_DISABLED);
        }
        return hybridStrategy.recommendArtworks(user);
    }

    private String buildUserPrompt(User user) {
        Map<Long, Set<String>> tagsByStore = new LinkedHashMap<>();
        for (Artwork artwork : artworkRepository.findAllByOrderByArtworkIdAsc()) {
            Set<String> tags = tagsByStore.computeIfAbsent(artwork.getStore().getStoreId(), id -> new LinkedHashSet<>());
            tags.addAll(ArtworkMapper.colorCodes(artwork));
            tags.addAll(ArtworkMapper.motifCodes(artwork));
        }
        StringBuilder prompt = new StringBuilder();
        prompt.append("Language: ").append(user.getPreferredLanguage() == Language.EN ? "English" : "Spanish")
                .append('\n');
        tasteProfileRepository.findByUserUserId(user.getUserId()).ifPresent((TasteProfile p) -> prompt
                .append("User preferences: ").append(UserMapper.toResponse(p)).append('\n'));
        prompt.append("Stores:\n");
        for (Store store : storeRepository.findAllWithMaster()) {
            prompt.append(store.getStoreId()).append(" | ").append(store.getNameEn())
                    .append(" | tags=").append(tagsByStore.getOrDefault(store.getStoreId(), Set.of())).append('\n');
        }
        return prompt.toString();
    }

    /** Parses lines "storeId|score|reason". Invalid lines are skipped. */
    static List<RecommendationResult> parse(String answer, Set<Long> validStores) {
        List<RecommendationResult> results = new ArrayList<>();
        if (answer == null) {
            return results;
        }
        Set<Long> seen = new LinkedHashSet<>();
        for (String line : answer.split("\\R")) {
            String[] parts = line.split("\\|", 3);
            if (parts.length < 3) {
                continue;
            }
            try {
                Long storeId = Long.valueOf(parts[0].trim());
                double score = Double.parseDouble(parts[1].trim());
                if (validStores.contains(storeId) && seen.add(storeId)) {
                    results.add(RecommendationResult.of(storeId, score, parts[2].trim()));
                }
            } catch (NumberFormatException ex) {
                log.debug("Ignoring AI line that cannot be parsed: {}", line);
            }
        }
        return results;
    }
}
