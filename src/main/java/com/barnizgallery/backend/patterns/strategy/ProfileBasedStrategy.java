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
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.TasteProfileRepository;

/**
 * <b>Strategy pattern – ConcreteStrategy "profile".</b>
 * <p>
 * Compares the user's taste profile (colors, shape, style, motifs and budget) with each available artwork
 * ({@link ProfileMatcher}). A store gets the score of its best matching artwork, and the reason says what
 * matched.
 */
@Component
public class ProfileBasedStrategy implements RecommendationStrategy {

    static final List<ArtworkStatus> AVAILABLE = List.of(ArtworkStatus.EXHIBITED, ArtworkStatus.IN_AUCTION);

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

    /** Available artworks that match some part of the profile, best first. */
    List<ProfileMatch> matches(User user) {
        Optional<TasteProfile> profile = tasteProfileRepository.findByUserUserId(user.getUserId());
        if (profile.isEmpty()) {
            return List.of();
        }
        List<ProfileMatch> matches = new ArrayList<>();
        for (Artwork artwork : artworkRepository.findByDeletedAtIsNullAndStatusInOrderByArtworkIdAsc(AVAILABLE,
                AiServiceStrategy.CANDIDATES)) {
            ProfileMatch match = ProfileMatcher.match(profile.get(), artwork);
            if (match.matchesSomething()) {
                matches.add(match);
            }
        }
        matches.sort(Comparator.comparingDouble(ProfileMatch::score).reversed());
        return matches;
    }
}
