package com.barnizgallery.backend.patterns.strategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.TasteProfile;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.repository.TasteProfileRepository;

/**
 * <b>Strategy pattern – ConcreteStrategy "profile".</b>
 * <p>
 * Compares the user's taste profile (colors, shape, style, motifs and budget) with each artwork of the public catalog
 * ({@link ProfileMatcher}). A store gets the score of its best matching artwork, and the reason says what
 * matched.
 */
@Component
public class ProfileBasedStrategy implements RecommendationStrategy {

    private final TasteProfileRepository tasteProfileRepository;
    private final PublicArtworkCatalog publicCatalog;

    public ProfileBasedStrategy(TasteProfileRepository tasteProfileRepository, PublicArtworkCatalog publicCatalog) {
        this.tasteProfileRepository = tasteProfileRepository;
        this.publicCatalog = publicCatalog;
    }

    @Override
    public String name() {
        return "profile";
    }

    @Override
    public List<RecommendationResult> recommend(User user) {
        Map<Long, ProfileMatch> bestByStore = new LinkedHashMap<>();
        for (ProfileMatch match : matches(user)) {
            bestByStore.merge(match.artwork().getStore().getStoreId(), match,
                    (current, other) -> other.score() > current.score() ? other : current);
        }
        List<RecommendationResult> results = new ArrayList<>();
        bestByStore.forEach((storeId, match) -> results.add(
                RecommendationResult.of(storeId, match.score(), ProfileMatcher.reason(user, match))));
        results.sort(Comparator.comparing(RecommendationResult::score).reversed());
        return results;
    }

    @Override
    public List<RecommendationResult> recommendArtworks(User user) {
        return matches(user).stream()
                .map(match -> RecommendationResult.ofArtwork(match.artwork().getArtworkId(), match.score(),
                        ProfileMatcher.reason(user, match)))
                .toList();
    }

    /**
     * Artworks of the public catalog that match a profile that is not saved (the visitor's kiosk); the reason is
     * written in the reader's language.
     */
    public List<RecommendationResult> recommendArtworksFor(TasteProfile profile, User reader) {
        return matches(profile).stream()
                .map(match -> RecommendationResult.ofArtwork(match.artwork().getArtworkId(), match.score(),
                        ProfileMatcher.reason(reader, match)))
                .toList();
    }

    /** Artworks of the public catalog that match some part of the profile, best first. */
    List<ProfileMatch> matches(User user) {
        Optional<TasteProfile> profile = tasteProfileRepository.findByUserUserId(user.getUserId());
        return profile.map(this::matches).orElse(List.of());
    }

    private List<ProfileMatch> matches(TasteProfile profile) {
        List<ProfileMatch> matches = new ArrayList<>();
        for (Artwork artwork : publicCatalog.artworks()) {
            ProfileMatch match = ProfileMatcher.match(profile, artwork);
            if (match.matchesSomething()) {
                matches.add(match);
            }
        }
        matches.sort(Comparator.comparingDouble(ProfileMatch::score).reversed());
        return matches;
    }
}
