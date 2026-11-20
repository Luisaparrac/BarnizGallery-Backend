package com.barnizgallery.backend.service;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.entity.BudgetRange;
import com.barnizgallery.backend.entity.Color;
import com.barnizgallery.backend.entity.Motif;
import com.barnizgallery.backend.entity.Shape;
import com.barnizgallery.backend.entity.Style;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.repository.BudgetRangeRepository;
import com.barnizgallery.backend.repository.ColorRepository;
import com.barnizgallery.backend.repository.MotifRepository;
import com.barnizgallery.backend.repository.ShapeRepository;
import com.barnizgallery.backend.repository.StyleRepository;

/**
 * Turns catalog codes sent by the clients (for example {@code "rojo"}) into catalog entities.
 * An unknown code is rejected with 422; nothing is created on the fly.
 */
@Component
public class CatalogCodeResolver {

    private final ColorRepository colorRepository;
    private final MotifRepository motifRepository;
    private final ShapeRepository shapeRepository;
    private final StyleRepository styleRepository;
    private final BudgetRangeRepository budgetRangeRepository;

    public CatalogCodeResolver(ColorRepository colorRepository, MotifRepository motifRepository,
            ShapeRepository shapeRepository, StyleRepository styleRepository,
            BudgetRangeRepository budgetRangeRepository) {
        this.colorRepository = colorRepository;
        this.motifRepository = motifRepository;
        this.shapeRepository = shapeRepository;
        this.styleRepository = styleRepository;
        this.budgetRangeRepository = budgetRangeRepository;
    }

    @Transactional(readOnly = true)
    public Set<Color> colors(Collection<String> codes) {
        return resolve(ErrorCode.UNKNOWN_COLOR_CODES, codes, colorRepository::findByCodeIn, Color::getCode);
    }

    @Transactional(readOnly = true)
    public Set<Motif> motifs(Collection<String> codes) {
        return resolve(ErrorCode.UNKNOWN_MOTIF_CODES, codes, motifRepository::findByCodeIn, Motif::getCode);
    }

    @Transactional(readOnly = true)
    public Set<Shape> shapes(Collection<String> codes) {
        return resolve(ErrorCode.UNKNOWN_SHAPE_CODES, codes, shapeRepository::findByCodeIn, Shape::getCode);
    }

    @Transactional(readOnly = true)
    public Set<Style> styles(Collection<String> codes) {
        return resolve(ErrorCode.UNKNOWN_STYLE_CODES, codes, styleRepository::findByCodeIn, Style::getCode);
    }

    /** Budget range with this code, or null when no code is given. */
    @Transactional(readOnly = true)
    public BudgetRange budgetRange(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return budgetRangeRepository.findByCode(code)
                .orElseThrow(() -> BusinessRuleException.unprocessable(ErrorCode.UNKNOWN_BUDGET_RANGE, code));
    }

    private static <T> Set<T> resolve(ErrorCode unknownCodes, Collection<String> codes,
            Function<Collection<String>, List<T>> finder, Function<T, String> codeOf) {
        if (codes == null || codes.isEmpty()) {
            return new LinkedHashSet<>();
        }
        Set<String> wanted = new LinkedHashSet<>(codes);
        List<T> found = finder.apply(wanted);
        Set<String> missing = new LinkedHashSet<>(wanted);
        found.forEach(item -> missing.remove(codeOf.apply(item)));
        if (!missing.isEmpty()) {
            throw BusinessRuleException.unprocessable(unknownCodes, String.join(", ", missing));
        }
        return new LinkedHashSet<>(found);
    }
}
