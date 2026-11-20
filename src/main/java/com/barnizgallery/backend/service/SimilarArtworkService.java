package com.barnizgallery.backend.service;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.AiSimilarItem;
import com.barnizgallery.backend.dto.AiSimilarRequest;
import com.barnizgallery.backend.dto.SimilarArtworkResponse;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.exception.AiServiceException;
import com.barnizgallery.backend.mapper.AiRequestMapper;
import com.barnizgallery.backend.patterns.adapter.AiServiceGateway;
import com.barnizgallery.backend.repository.ArtworkCatalogViewRepository;
import com.barnizgallery.backend.repository.ArtworkRepository;

/**
 * "Similar artworks" for an artwork of the public gallery. The AI service ranks them when it is on; if it is off
 * or fails, the fixed {@link SimilarityRules} answer instead, so the page always shows something.
 */
@Service
public class SimilarArtworkService {

    static final int LIMIT = 6;
    private static final int MAX_CANDIDATES = 500;
    private static final Logger log = LoggerFactory.getLogger(SimilarArtworkService.class);

    private final ArtworkService artworkService;
    private final ArtworkRepository artworkRepository;
    private final ArtworkCatalogViewRepository catalogRepository;
    private final AiServiceGateway aiService;

    public SimilarArtworkService(ArtworkService artworkService, ArtworkRepository artworkRepository,
            ArtworkCatalogViewRepository catalogRepository, AiServiceGateway aiService) {
        this.artworkService = artworkService;
        this.artworkRepository = artworkRepository;
        this.catalogRepository = catalogRepository;
        this.aiService = aiService;
    }

    @Transactional(readOnly = true)
    public List<SimilarArtworkResponse> similar(Long artworkId) {
        Artwork base = artworkService.requirePublic(artworkId);
        Set<Long> publicIds = new HashSet<>(catalogRepository.findAllArtworkIds());
        List<Artwork> candidates = artworkRepository.findByDeletedAtIsNullAndStatusInOrderByArtworkIdAsc(
                ArtworkStatus.PUBLIC, PageRequest.of(0, MAX_CANDIDATES)).stream()
                .filter(a -> publicIds.contains(a.getArtworkId()) && !a.getArtworkId().equals(artworkId))
                .toList();
        if (candidates.isEmpty()) {
            return List.of();
        }
        Locale locale = LocaleContextHolder.getLocale();
        if (aiService.isAvailable()) {
            try {
                return fromAi(base, candidates, locale);
            } catch (AiServiceException ex) {
                log.warn("Similar artworks of {} without the AI service: {}", artworkId, ex.getCode());
            }
        }
        return candidates.stream()
                .map(c -> response(c, SimilarityRules.score(base, c), SimilarityRules.reason(base, c, locale), "rules"))
                .filter(r -> r.score() > 0)
                .sorted(Comparator.comparingDouble(SimilarArtworkResponse::score).reversed())
                .limit(LIMIT)
                .toList();
    }

    private List<SimilarArtworkResponse> fromAi(Artwork base, List<Artwork> candidates, Locale locale) {
        Map<Long, Artwork> byId = candidates.stream().collect(Collectors.toMap(Artwork::getArtworkId, Function.identity()));
        AiSimilarRequest request = new AiSimilarRequest(AiRequestMapper.toRef(base),
                candidates.stream().map(AiRequestMapper::toRef).toList(), LIMIT);
        List<AiSimilarItem> items = aiService.similar(request).items();
        return (items == null ? List.<AiSimilarItem>of() : items).stream()
                .filter(item -> item.score() > 0 && byId.containsKey(item.artworkId()))
                .limit(LIMIT)
                .map(item -> {
                    Artwork artwork = byId.get(item.artworkId());
                    return response(artwork, item.score() * 100, reason(base, artwork, item, locale), "ai");
                })
                .toList();
    }

    /** The service explains in Spanish only: other languages get the reason of the fixed rules. */
    private static String reason(Artwork base, Artwork artwork, AiSimilarItem item, Locale locale) {
        boolean spanish = !Locale.ENGLISH.getLanguage().equals(locale.getLanguage());
        if (spanish && item.explanation() != null && !item.explanation().isBlank()) {
            return item.explanation().strip();
        }
        return SimilarityRules.reason(base, artwork, locale);
    }

    private static SimilarArtworkResponse response(Artwork artwork, double score, String reason, String source) {
        return new SimilarArtworkResponse(artwork.getArtworkId(), artwork.getStore().getStoreId(),
                artwork.getTitleEs(), artwork.getTitleEn(), artwork.getPrice(), artwork.getCurrency(),
                Math.round(score * 10) / 10.0, reason, source);
    }
}
