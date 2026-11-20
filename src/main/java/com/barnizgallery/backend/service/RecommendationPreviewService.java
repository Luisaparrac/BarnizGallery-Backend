package com.barnizgallery.backend.service;

import java.time.Clock;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.RecommendationPreviewResponse;
import com.barnizgallery.backend.dto.TastePreviewRequest;
import com.barnizgallery.backend.entity.TasteProfile;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.patterns.strategy.ProfileBasedStrategy;
import com.barnizgallery.backend.repository.BudgetRangeRepository;
import com.barnizgallery.backend.repository.ColorRepository;
import com.barnizgallery.backend.repository.MotifRepository;
import com.barnizgallery.backend.repository.ShapeRepository;
import com.barnizgallery.backend.repository.StyleRepository;

/**
 * Recommendations for the visitor's taste kiosk (no account): the answers are compared with the artworks of the
 * public catalog by the profile strategy, in the request language, and nothing is saved. Unknown catalog ids
 * answer 422.
 */
@Service
public class RecommendationPreviewService {

    static final int MAX_RESULTS = 20;

    private final ProfileBasedStrategy profileStrategy;
    private final ColorRepository colorRepository;
    private final ShapeRepository shapeRepository;
    private final StyleRepository styleRepository;
    private final MotifRepository motifRepository;
    private final BudgetRangeRepository budgetRangeRepository;
    private final Clock clock;

    public RecommendationPreviewService(ProfileBasedStrategy profileStrategy, ColorRepository colorRepository,
            ShapeRepository shapeRepository, StyleRepository styleRepository, MotifRepository motifRepository,
            BudgetRangeRepository budgetRangeRepository, Clock clock) {
        this.profileStrategy = profileStrategy;
        this.colorRepository = colorRepository;
        this.shapeRepository = shapeRepository;
        this.styleRepository = styleRepository;
        this.motifRepository = motifRepository;
        this.budgetRangeRepository = budgetRangeRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<RecommendationPreviewResponse> preview(TastePreviewRequest request) {
        TasteProfile profile = new TasteProfile();
        profile.getColors().addAll(load(colorRepository, request.colorIds()));
        profile.getShapes().addAll(load(shapeRepository, request.shapeIds()));
        profile.getStyles().addAll(load(styleRepository, request.styleIds()));
        profile.getMotifs().addAll(load(motifRepository, request.motifIds()));
        if (request.budgetRangeId() != null) {
            profile.setBudgetRange(budgetRangeRepository.findById(request.budgetRangeId())
                    .orElseThrow(() -> BusinessRuleException.unprocessable(ErrorCode.UNKNOWN_CATALOG_ID)));
        }
        User reader = new User();
        reader.setPreferredLanguage(Locale.ENGLISH.getLanguage().equals(LocaleContextHolder.getLocale().getLanguage())
                ? Language.EN : Language.ES);
        return profileStrategy.recommendArtworksFor(profile, reader).stream()
                .limit(MAX_RESULTS)
                .map(r -> new RecommendationPreviewResponse(null, null, r.artworkId(), r.score(), r.reason(),
                        profileStrategy.name(), clock.instant()))
                .toList();
    }

    private static <T> Collection<T> load(JpaRepository<T, Long> repository, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        Set<Long> wanted = Set.copyOf(ids);
        List<T> found = repository.findAllById(wanted);
        if (found.size() != wanted.size()) {
            throw BusinessRuleException.unprocessable(ErrorCode.UNKNOWN_CATALOG_ID);
        }
        return found.stream().map(Function.<T>identity()).toList();
    }
}
