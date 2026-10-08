package com.barnizgallery.backend.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.request.RoomRequest;
import com.barnizgallery.backend.dto.response.ArtworkSummaryResponse;
import com.barnizgallery.backend.dto.response.RoomDetailResponse;
import com.barnizgallery.backend.dto.response.RoomSummaryResponse;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.mapper.ArtworkMapper;
import com.barnizgallery.backend.mapper.RoomMapper;
import com.barnizgallery.backend.model.entity.Master;
import com.barnizgallery.backend.model.entity.Room;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.ArtworkRepository.RoomArtworkCount;
import com.barnizgallery.backend.repository.RoomRepository;

/**
 * Business logic for rooms. Each master has at most one room (1:1).
 * Rooms cannot be deleted from the API.
 */
@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final ArtworkRepository artworkRepository;
    private final MasterService masterService;

    public RoomService(RoomRepository roomRepository, ArtworkRepository artworkRepository,
            MasterService masterService) {
        this.roomRepository = roomRepository;
        this.artworkRepository = artworkRepository;
        this.masterService = masterService;
    }

    @Transactional(readOnly = true)
    public List<RoomSummaryResponse> findAll() {
        Map<Integer, Long> counts = artworkRepository.countGroupedByRoom().stream()
                .collect(Collectors.toMap(RoomArtworkCount::getRoomId, RoomArtworkCount::getArtworkCount));
        return roomRepository.findAllWithMaster().stream()
                .map(room -> RoomMapper.toSummary(room, counts.getOrDefault(room.getRoomId(), 0L)))
                .toList();
    }

    @Transactional(readOnly = true)
    public RoomDetailResponse findById(Integer roomId) {
        Room room = roomRepository.findByIdWithMaster(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));
        List<ArtworkSummaryResponse> artworks = artworkRepository.findByRoomRoomIdOrderByArtworkIdAsc(roomId)
                .stream()
                .map(ArtworkMapper::toSummary)
                .toList();
        return RoomMapper.toDetail(room, artworks);
    }

    @Transactional
    public RoomSummaryResponse create(RoomRequest request) {
        Master master = masterService.getMaster(request.masterId());
        if (roomRepository.existsByMasterMasterId(master.getMasterId())) {
            throw new BusinessRuleException("Master " + master.getMasterId() + " already has a room");
        }
        Room room = new Room();
        room.setMaster(master);
        RoomMapper.apply(request, room);
        return RoomMapper.toSummary(roomRepository.save(room), 0);
    }

    @Transactional
    public RoomSummaryResponse update(Integer roomId, RoomRequest request) {
        Room room = getRoom(roomId);
        if (!room.getMaster().getMasterId().equals(request.masterId())) {
            Master newMaster = masterService.getMaster(request.masterId());
            if (roomRepository.existsByMasterMasterId(newMaster.getMasterId())) {
                throw new BusinessRuleException("Master " + newMaster.getMasterId() + " already has a room");
            }
            room.setMaster(newMaster);
        }
        RoomMapper.apply(request, room);
        return RoomMapper.toSummary(room, artworkRepository.countByRoomRoomId(roomId));
    }

    /** Loads a room or throws 404. Used by other services too. */
    @Transactional(readOnly = true)
    public Room getRoom(Integer roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));
    }
}
