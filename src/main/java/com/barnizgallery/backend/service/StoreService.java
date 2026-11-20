package com.barnizgallery.backend.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.ArtworkSummaryResponse;
import com.barnizgallery.backend.dto.StoreDetailResponse;
import com.barnizgallery.backend.dto.StoreRequest;
import com.barnizgallery.backend.dto.StoreSummaryResponse;
import com.barnizgallery.backend.entity.Master;
import com.barnizgallery.backend.entity.Store;
import com.barnizgallery.backend.entity.StoreTemplate;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.mapper.ArtworkMapper;
import com.barnizgallery.backend.mapper.StoreMapper;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.StoreArtworkCount;
import com.barnizgallery.backend.repository.StoreRepository;
import com.barnizgallery.backend.repository.StoreTemplateRepository;

/**
 * Business logic for stores. Each master has at most one store (1:1).
 * Stores cannot be deleted from the API.
 */
@Service
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreTemplateRepository templateRepository;
    private final ArtworkRepository artworkRepository;
    private final MasterService masterService;

    public StoreService(StoreRepository storeRepository, StoreTemplateRepository templateRepository,
            ArtworkRepository artworkRepository, MasterService masterService) {
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

    @Transactional
    public StoreSummaryResponse create(StoreRequest request) {
        Master master = masterService.getMaster(request.masterId());
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
        if (!store.getMaster().getMasterId().equals(request.masterId())) {
            Master newMaster = masterService.getMaster(request.masterId());
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
