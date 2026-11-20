package com.barnizgallery.backend.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.ArtworkSummaryResponse;
import com.barnizgallery.backend.dto.StorePageResponse;
import com.barnizgallery.backend.dto.StoreSectionResponse;
import com.barnizgallery.backend.dto.StoreSummaryResponse;
import com.barnizgallery.backend.entity.Store;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.enums.MasterVerificationStatus;
import com.barnizgallery.backend.enums.StoreStatus;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.mapper.ArtworkMapper;
import com.barnizgallery.backend.mapper.StoreEditorMapper;
import com.barnizgallery.backend.mapper.StoreMapper;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.StoreArtworkCount;
import com.barnizgallery.backend.repository.StoreRepository;
import com.barnizgallery.backend.repository.StoreSectionRepository;

/**
 * Public side of the stores: the published stores and their pages. Artisans edit their own store through
 * {@link StoreEditorService}; stores cannot be deleted from the API.
 */
@Service
public class StoreService {

    private final StoreRepository storeRepository;
    private final ArtworkRepository artworkRepository;
    private final StoreSectionRepository sectionRepository;

    public StoreService(StoreRepository storeRepository, ArtworkRepository artworkRepository,
            StoreSectionRepository sectionRepository) {
        this.storeRepository = storeRepository;
        this.artworkRepository = artworkRepository;
        this.sectionRepository = sectionRepository;
    }

    /** Published stores of approved masters, newest first, with their number of public artworks. */
    @Transactional(readOnly = true)
    public List<StoreSummaryResponse> findPublished() {
        Map<Long, Long> counts = artworkRepository.countGroupedByStore(ArtworkStatus.PUBLIC).stream()
                .collect(Collectors.toMap(StoreArtworkCount::getStoreId, StoreArtworkCount::getArtworkCount));
        return storeRepository.findByStatusWithMaster(StoreStatus.PUBLISHED, MasterVerificationStatus.APPROVED).stream()
                .map(store -> StoreMapper.toSummary(store, counts.getOrDefault(store.getStoreId(), 0L)))
                .toList();
    }

    /**
     * Public page of a store by its slug. Only published stores of approved masters exist for the public (404
     * otherwise); hidden sections and artworks that are not exhibited or in auction are left out.
     */
    @Transactional(readOnly = true)
    public StorePageResponse findPublishedBySlug(String slug) {
        Store store = storeRepository.findBySlugAndStatus(slug, StoreStatus.PUBLISHED, MasterVerificationStatus.APPROVED)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.STORE_NOT_FOUND, slug));
        List<StoreSectionResponse> sections = sectionRepository
                .findByStoreStoreIdAndVisibleTrueOrderBySortOrderAscStoreSectionIdAsc(store.getStoreId()).stream()
                .map(StoreEditorMapper::toResponse)
                .toList();
        List<ArtworkSummaryResponse> artworks = artworkRepository
                .findByStoreStoreIdAndDeletedAtIsNullAndStatusInOrderByArtworkIdAsc(store.getStoreId(),
                        ArtworkStatus.PUBLIC).stream()
                .map(ArtworkMapper::toSummary)
                .toList();
        return StoreMapper.toPage(store, sections, artworks);
    }

    /** Loads a store or throws 404. Used by other services too. */
    @Transactional(readOnly = true)
    public Store getStore(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.STORE_NOT_FOUND, storeId));
    }
}
