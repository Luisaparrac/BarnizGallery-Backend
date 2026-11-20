package com.barnizgallery.backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.RecommendationResponse;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Recommendation;
import com.barnizgallery.backend.entity.Store;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.mapper.InteractionMapper;
import com.barnizgallery.backend.patterns.facade.AiFacade;
import com.barnizgallery.backend.patterns.strategy.RecommendationResult;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.RecommendationRepository;
import com.barnizgallery.backend.repository.StoreRepository;

/**
 * Computes and stores store recommendations. The algorithm is chosen through the
 * {@link AiFacade} (which uses the Strategy pattern).
 */
@Service
public class RecommendationService {

    public static final String STORES = "stores";
    public static final String ARTWORKS = "artworks";

    private final AiFacade aiFacade;
    private final RecommendationRepository recommendationRepository;
    private final StoreRepository storeRepository;
    private final UserService userService;
    private final ArtworkRepository artworkRepository;

    public RecommendationService(AiFacade aiFacade, RecommendationRepository recommendationRepository,
            StoreRepository storeRepository, UserService userService, ArtworkRepository artworkRepository) {
        this.aiFacade = aiFacade;
        this.recommendationRepository = recommendationRepository;
        this.storeRepository = storeRepository;
        this.userService = userService;
        this.artworkRepository = artworkRepository;
    }

    /**
     * Computes new recommendations of stores or of artworks ({@code target} = {@value #STORES} or
     * {@value #ARTWORKS}), replaces the previous ones of the same target and returns them by score.
     */
    @Transactional
    public List<RecommendationResponse> compute(Long userId, String strategyName, String target) {
        User user = userService.getUser(userId);
        boolean artworks = isArtworkTarget(target);
        List<RecommendationResult> results = artworks ? aiFacade.recommendArtworks(user, strategyName)
                : aiFacade.recommendStores(user, strategyName);
        if (artworks) {
            recommendationRepository.deleteArtworksByUserId(userId);
        } else {
            recommendationRepository.deleteStoresByUserId(userId);
        }
        List<Long> ids = results.stream().map(RecommendationResult::targetId).toList();
        Map<Long, Store> stores = artworks ? Map.of() : storeRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Store::getStoreId, Function.identity()));
        Map<Long, Artwork> artworkMap = artworks ? artworkRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Artwork::getArtworkId, Function.identity())) : Map.of();

        String strategy = aiFacade.strategyName(strategyName);
        LocalDateTime now = LocalDateTime.now();
        List<Recommendation> saved = new ArrayList<>();
        for (RecommendationResult result : results) {
            Recommendation recommendation = new Recommendation();
            recommendation.setUser(user);
            if (artworks) {
                recommendation.setArtwork(artworkMap.get(result.artworkId()));
            } else {
                recommendation.setStore(stores.get(result.storeId()));
            }
            if (recommendation.getArtwork() == null && recommendation.getStore() == null) {
                continue;
            }
            recommendation.setStrategy(strategy);
            recommendation.setScore(result.score());
            recommendation.setReason(result.reason());
            recommendation.setRecommendationDate(now);
            saved.add(recommendation);
        }
        return recommendationRepository.saveAll(saved).stream()
                .map(InteractionMapper::toResponse)
                .toList();
    }

    private static boolean isArtworkTarget(String target) {
        if (target == null || STORES.equals(target)) {
            return false;
        }
        if (ARTWORKS.equals(target)) {
            return true;
        }
        throw BusinessRuleException.badRequest(ErrorCode.UNKNOWN_RECOMMENDATION_TARGET, target);
    }

    @Transactional(readOnly = true)
    public List<RecommendationResponse> latest(Long userId) {
        userService.getUser(userId);
        return recommendationRepository.findByUserWithTarget(userId).stream()
                .map(InteractionMapper::toResponse)
                .toList();
    }
}
