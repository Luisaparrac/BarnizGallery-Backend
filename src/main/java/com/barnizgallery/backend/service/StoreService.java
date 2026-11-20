package com.barnizgallery.backend.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.ArtworkSummaryResponse;
import com.barnizgallery.backend.dto.StoreDetailResponse;
import com.barnizgallery.backend.dto.StorePageResponse;
import com.barnizgallery.backend.dto.StoreRequest;
import com.barnizgallery.backend.dto.StoreSectionResponse;
import com.barnizgallery.backend.dto.StoreSummaryResponse;
import com.barnizgallery.backend.entity.Master;
import com.barnizgallery.backend.entity.Store;
import com.barnizgallery.backend.entity.StoreTemplate;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.enums.MasterVerificationStatus;
import com.barnizgallery.backend.enums.StoreStatus;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.mapper.ArtworkMapper;
import com.barnizgallery.backend.mapper.StoreEditorMapper;
import com.barnizgallery.backend.mapper.StoreMapper;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.StoreArtworkCount;
import com.barnizgallery.backend.repository.StoreRepository;
import com.barnizgallery.backend.repository.StoreSectionRepository;
import com.barnizgallery.backend.repository.StoreTemplateRepository;
import com.barnizgallery.backend.security.OwnershipGuard;

/**
 * Business logic for stores. Each master has at most one store (1:1).
 * Stores cannot be deleted from the API.
 */
@Service
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreTemplateRepository templateRepository;
    private final ArtworkRepository artworkRepository;
    private final StoreSectionRepository sectionRepository;
    private final MasterService masterService;
    private final OwnershipGuard ownershipGuard;

    public StoreService(StoreRepository storeRepository, StoreTemplateRepository templateRepository,
            ArtworkRepository artworkRepository, StoreSectionRepository sectionRepository, MasterService masterService,
            OwnershipGuard ownershipGuard) {
        this.sectionRepository = sectionRepository;
        this.ownershipGuard = ownershipGuard;
        this.storeRepository = storeRepository;
        this.templateRepository = templateRepository;
        this.artworkRepository = artworkRepository;
        this.masterService = masterService;
    }

    @Transactional(readOnly = true)
    public List<StoreSummaryResponse> findAll() {
        Map<Long, Long> counts = artworkRepository.countGroupedByStore().stream()
                .collect(Collectors.toMap(StoreArtworkCount::getStoreId, StoreArtworkCount::getArtworkCount));
        return storeRepository.findAllWithMaster().stream()
                .map(store -> StoreMapper.toSummary(store, counts.getOrDefault(store.getStoreId(), 0L)))
                .toList();
    }

    @Transactional(readOnly = true)
    public StoreDetailResponse findById(Long storeId) {
        Store store = storeRepository.findByIdWithMaster(storeId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.STORE_NOT_FOUND, storeId));
        List<ArtworkSummaryResponse> artworks = artworkRepository.findByStoreStoreIdOrderByArtworkIdAsc(storeId)
                .stream()
                .map(ArtworkMapper::toSummary)
                .toList();
        return StoreMapper.toDetail(store, artworks);
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

    @Transactional
    public StoreSummaryResponse create(StoreRequest request) {
        Master master = masterService.getMaster(request.masterId());
        ownershipGuard.requireMaster(master);
        if (storeRepository.existsByMasterMasterId(master.getMasterId())) {
            throw new BusinessRuleException(ErrorCode.MASTER_ALREADY_HAS_STORE, master.getMasterId());
        }
        Store store = new Store();
        store.setMaster(master);
        store.setTemplate(getTemplate(request.templateId()));
        StoreMapper.apply(request, store);
        return StoreMapper.toSummary(storeRepository.save(store), 0);
    }

    @Transactional
    public StoreSummaryResponse update(Long storeId, StoreRequest request) {
        Store store = getStore(storeId);
        ownershipGuard.requireStore(store);
        if (!store.getMaster().getMasterId().equals(request.masterId())) {
            Master newMaster = masterService.getMaster(request.masterId());
            ownershipGuard.requireMaster(newMaster);
            if (storeRepository.existsByMasterMasterId(newMaster.getMasterId())) {
                throw new BusinessRuleException(ErrorCode.MASTER_ALREADY_HAS_STORE, newMaster.getMasterId());
            }
            store.setMaster(newMaster);
        }
        if (!store.getTemplate().getTemplateId().equals(request.templateId())) {
            store.setTemplate(getTemplate(request.templateId()));
        }
        StoreMapper.apply(request, store);
        return StoreMapper.toSummary(store, artworkRepository.countByStoreStoreId(storeId));
    }

    private StoreTemplate getTemplate(Long templateId) {
        return templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.STORE_TEMPLATE_NOT_FOUND, templateId));
    }

    /** Loads a store or throws 404. Used by other services too. */
    @Transactional(readOnly = true)
    public Store getStore(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.STORE_NOT_FOUND, storeId));
    }
}
