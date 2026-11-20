package com.barnizgallery.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.BudgetRangeResponse;
import com.barnizgallery.backend.dto.CatalogEntryResponse;
import com.barnizgallery.backend.dto.CatalogsResponse;
import com.barnizgallery.backend.dto.StoreTemplateResponse;
import com.barnizgallery.backend.entity.CatalogEntry;
import com.barnizgallery.backend.mapper.CatalogMapper;
import com.barnizgallery.backend.repository.AvatarRepository;
import com.barnizgallery.backend.repository.BudgetRangeRepository;
import com.barnizgallery.backend.repository.CatalogEntryRepository;
import com.barnizgallery.backend.repository.ColorRepository;
import com.barnizgallery.backend.repository.MotifRepository;
import com.barnizgallery.backend.repository.ShapeRepository;
import com.barnizgallery.backend.repository.StoreTemplateRepository;
import com.barnizgallery.backend.repository.StyleRepository;

/**
 * Public view of the catalogs: only the active entries, in list order. An empty catalog is an empty list.
 */
@Service
public class CatalogService {

    private final ColorRepository colorRepository;
    private final ShapeRepository shapeRepository;
    private final MotifRepository motifRepository;
    private final StyleRepository styleRepository;
    private final BudgetRangeRepository budgetRangeRepository;
    private final AvatarRepository avatarRepository;
    private final StoreTemplateRepository templateRepository;

    public CatalogService(ColorRepository colorRepository, ShapeRepository shapeRepository,
            MotifRepository motifRepository, StyleRepository styleRepository,
            BudgetRangeRepository budgetRangeRepository, AvatarRepository avatarRepository,
            StoreTemplateRepository templateRepository) {
        this.colorRepository = colorRepository;
        this.shapeRepository = shapeRepository;
        this.motifRepository = motifRepository;
        this.styleRepository = styleRepository;
        this.budgetRangeRepository = budgetRangeRepository;
        this.avatarRepository = avatarRepository;
        this.templateRepository = templateRepository;
    }

    @Transactional(readOnly = true)
    public CatalogsResponse activeCatalogs() {
        List<BudgetRangeResponse> budgets = budgetRangeRepository.findAllByOrderByMinAmountAscCodeAsc().stream()
                .map(CatalogMapper::toResponse).toList();
        List<StoreTemplateResponse> templates = templateRepository.findByActiveTrueOrderByCodeAsc().stream()
                .map(CatalogMapper::toResponse).toList();
        return new CatalogsResponse(active(colorRepository), active(shapeRepository), active(motifRepository),
                active(styleRepository), budgets, active(avatarRepository), templates, budgets, templates);
    }

    private static <E extends CatalogEntry> List<CatalogEntryResponse> active(CatalogEntryRepository<E> repository) {
        return repository.findByActiveTrueOrderBySortOrderAscCodeAsc().stream()
                .map(CatalogMapper::toResponse)
                .toList();
    }
}
