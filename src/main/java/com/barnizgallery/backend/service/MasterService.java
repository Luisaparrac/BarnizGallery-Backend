package com.barnizgallery.backend.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.MasterDetailResponse;
import com.barnizgallery.backend.dto.MasterRequest;
import com.barnizgallery.backend.dto.MasterResponse;
import com.barnizgallery.backend.dto.StoreSummaryResponse;
import com.barnizgallery.backend.entity.Master;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.mapper.MasterMapper;
import com.barnizgallery.backend.mapper.StoreMapper;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.MasterRepository;
import com.barnizgallery.backend.repository.StoreRepository;

/**
 * Business logic for masters. Masters cannot be deleted from the API
 * (the database cascades the delete to stores, artworks, auctions and bids).
 */
@Service
public class MasterService {

    private final MasterRepository masterRepository;
    private final StoreRepository storeRepository;
    private final ArtworkRepository artworkRepository;

    public MasterService(MasterRepository masterRepository, StoreRepository storeRepository,
            ArtworkRepository artworkRepository) {
        this.masterRepository = masterRepository;
        this.storeRepository = storeRepository;
        this.artworkRepository = artworkRepository;
    }

    @Transactional(readOnly = true)
    public List<MasterResponse> findAll() {
        return masterRepository.findAll(Sort.by("masterId")).stream()
                .map(MasterMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MasterDetailResponse findById(Long masterId) {
        Master master = getMaster(masterId);
        StoreSummaryResponse store = storeRepository.findByMasterMasterId(masterId)
                .map(r -> StoreMapper.toSummary(r, artworkRepository.countByStoreStoreId(r.getStoreId())))
                .orElse(null);
        return MasterMapper.toDetail(master, store);
    }

    @Transactional
    public MasterResponse create(MasterRequest request) {
        Master master = new Master();
        MasterMapper.apply(request, master);
        return MasterMapper.toResponse(masterRepository.save(master));
    }

    @Transactional
    public MasterResponse update(Long masterId, MasterRequest request) {
        Master master = getMaster(masterId);
        MasterMapper.apply(request, master);
        return MasterMapper.toResponse(master);
    }

    /** Loads a master or throws 404. Used by other services too. */
    @Transactional(readOnly = true)
    public Master getMaster(Long masterId) {
        return masterRepository.findById(masterId)
                .orElseThrow(() -> new ResourceNotFoundException("Master", masterId));
    }
}
