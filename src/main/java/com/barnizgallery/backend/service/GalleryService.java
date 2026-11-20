package com.barnizgallery.backend.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.ArtworkFilter;
import com.barnizgallery.backend.dto.CatalogArtworkResponse;
import com.barnizgallery.backend.dto.GalleryNodeResponse;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.ArtworkCatalogView;
import com.barnizgallery.backend.entity.Photo;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.mapper.ArtworkDataMapper;
import com.barnizgallery.backend.mapper.CatalogArtworkMapper;
import com.barnizgallery.backend.repository.ArtworkCatalogViewRepository;
import com.barnizgallery.backend.repository.ArtworkFavoriteCount;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.FavoriteRepository;
import com.barnizgallery.backend.repository.PhotoRepository;

/**
 * The public gallery: the whole tree (Composite pattern) and the filtered list of artworks.
 */
@Service
public class GalleryService {

    static final int DEFAULT_PAGE_SIZE = 24;

    private final GalleryTreeBuilder treeBuilder;
    private final ArtworkCatalogViewRepository catalogRepository;
    private final ArtworkRepository artworkRepository;
    private final PhotoRepository photoRepository;
    private final FavoriteRepository favoriteRepository;

    public GalleryService(GalleryTreeBuilder treeBuilder, ArtworkCatalogViewRepository catalogRepository,
            ArtworkRepository artworkRepository, PhotoRepository photoRepository,
            FavoriteRepository favoriteRepository) {
        this.treeBuilder = treeBuilder;
        this.catalogRepository = catalogRepository;
        this.artworkRepository = artworkRepository;
        this.photoRepository = photoRepository;
        this.favoriteRepository = favoriteRepository;
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
        List<ArtworkCatalogView> rows = catalogRepository.search(filter.color(), filter.motif(), filter.shape(),
                filter.style(), filter.currency(), filter.minPrice(), filter.maxPrice(), filter.store(),
                filter.search() == null || filter.search().isBlank() ? null : filter.search().strip(), filter.sort(),
                size, page * size);
        List<Long> ids = rows.stream().map(ArtworkCatalogView::getArtworkId).toList();
        Map<Long, Artwork> artworks = artworkRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Artwork::getArtworkId, Function.identity()));
        Map<Long, List<Photo>> photos = photoRepository.findByArtworkArtworkIdInOrderBySortOrderAscPhotoIdAsc(ids)
                .stream().collect(Collectors.groupingBy(p -> p.getArtwork().getArtworkId()));
        Map<Long, Long> favorites = favoriteRepository.countByArtworks(ids).stream()
                .collect(Collectors.toMap(ArtworkFavoriteCount::getArtworkId, ArtworkFavoriteCount::getTotal));
        return rows.stream()
                .map(row -> CatalogArtworkMapper.toResponse(row,
                        ArtworkDataMapper.toData(artworks.get(row.getArtworkId()),
                                photos.getOrDefault(row.getArtworkId(), List.of())),
                        favorites.getOrDefault(row.getArtworkId(), 0L)))
                .toList();
    }
}
