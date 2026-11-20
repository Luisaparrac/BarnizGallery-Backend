package com.barnizgallery.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.ArtworkFilter;
import com.barnizgallery.backend.dto.CatalogArtworkResponse;
import com.barnizgallery.backend.dto.GalleryNodeResponse;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.mapper.CatalogArtworkMapper;
import com.barnizgallery.backend.repository.ArtworkCatalogViewRepository;

/**
 * The public gallery: the whole tree (Composite pattern) and the filtered list of artworks.
 */
@Service
public class GalleryService {

    static final int DEFAULT_PAGE_SIZE = 24;

    private final GalleryTreeBuilder treeBuilder;
    private final ArtworkCatalogViewRepository catalogRepository;

    public GalleryService(GalleryTreeBuilder treeBuilder, ArtworkCatalogViewRepository catalogRepository) {
        this.treeBuilder = treeBuilder;
        this.catalogRepository = catalogRepository;
    }

    @Transactional(readOnly = true)
    public GalleryNodeResponse getGallery() {
        return treeBuilder.build().toNode();
    }

    /** Public artworks matching the filters, newest first; a price range without currency → 422. */
    @Transactional(readOnly = true)
    public List<CatalogArtworkResponse> artworks(ArtworkFilter filter) {
        if ((filter.minPrice() != null || filter.maxPrice() != null) && filter.currency() == null) {
            throw BusinessRuleException.unprocessable(ErrorCode.PRICE_FILTER_CURRENCY_REQUIRED);
        }
        int size = filter.size() == null ? DEFAULT_PAGE_SIZE : filter.size();
        int page = filter.page() == null ? 0 : filter.page();
        return catalogRepository.search(filter.color(), filter.motif(), filter.shape(), filter.style(),
                filter.currency(), filter.minPrice(), filter.maxPrice(), filter.store(), size, page * size).stream()
                .map(CatalogArtworkMapper::toResponse)
                .toList();
    }
}
