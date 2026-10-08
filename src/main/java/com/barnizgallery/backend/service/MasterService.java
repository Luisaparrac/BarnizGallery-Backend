package com.barnizgallery.backend.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.MasterDetailResponse;
import com.barnizgallery.backend.dto.MasterRequest;
import com.barnizgallery.backend.dto.MasterResponse;
import com.barnizgallery.backend.dto.RoomSummaryResponse;
import com.barnizgallery.backend.entity.Master;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.mapper.MasterMapper;
import com.barnizgallery.backend.mapper.RoomMapper;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.MasterRepository;
import com.barnizgallery.backend.repository.RoomRepository;

/**
 * Business logic for masters. Masters cannot be deleted from the API
 * (the database cascades the delete to rooms, artworks, auctions and bids).
 */
@Service
public class MasterService {

    private final MasterRepository masterRepository;
    private final RoomRepository roomRepository;
    private final ArtworkRepository artworkRepository;

    public MasterService(MasterRepository masterRepository, RoomRepository roomRepository,
            ArtworkRepository artworkRepository) {
        this.masterRepository = masterRepository;
        this.roomRepository = roomRepository;
        this.artworkRepository = artworkRepository;
    }

    @Transactional(readOnly = true)
    public List<MasterResponse> findAll() {
        return masterRepository.findAll(Sort.by("masterId")).stream()
                .map(MasterMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MasterDetailResponse findById(Integer masterId) {
        Master master = getMaster(masterId);
        RoomSummaryResponse room = roomRepository.findByMasterMasterId(masterId)
                .map(r -> RoomMapper.toSummary(r, artworkRepository.countByRoomRoomId(r.getRoomId())))
                .orElse(null);
        return MasterMapper.toDetail(master, room);
    }

    @Transactional
    public MasterResponse create(MasterRequest request) {
        Master master = new Master();
        MasterMapper.apply(request, master);
        return MasterMapper.toResponse(masterRepository.save(master));
    }

    @Transactional
    public MasterResponse update(Integer masterId, MasterRequest request) {
        Master master = getMaster(masterId);
        MasterMapper.apply(request, master);
        return MasterMapper.toResponse(master);
    }

    /** Loads a master or throws 404. Used by other services too. */
    @Transactional(readOnly = true)
    public Master getMaster(Integer masterId) {
        return masterRepository.findById(masterId)
                .orElseThrow(() -> new ResourceNotFoundException("Master", masterId));
    }
}
