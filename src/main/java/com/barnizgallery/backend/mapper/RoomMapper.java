package com.barnizgallery.backend.mapper;

import java.util.List;

import com.barnizgallery.backend.dto.request.RoomRequest;
import com.barnizgallery.backend.dto.response.ArtworkSummaryResponse;
import com.barnizgallery.backend.dto.response.RoomDetailResponse;
import com.barnizgallery.backend.dto.response.RoomSummaryResponse;
import com.barnizgallery.backend.model.entity.Master;
import com.barnizgallery.backend.model.entity.Room;

/**
 * Converts between {@link Room} and its DTOs.
 */
public final class RoomMapper {

    private RoomMapper() {
    }

    public static RoomSummaryResponse toSummary(Room room, long artworkCount) {
        Master master = room.getMaster();
        return new RoomSummaryResponse(room.getRoomId(), master.getMasterId(), master.getName(), room.getNameEs(),
                room.getNameEn(), room.getDescriptionEs(), room.getDescriptionEn(), room.getScene3dUrl(),
                artworkCount);
    }

    public static RoomDetailResponse toDetail(Room room, List<ArtworkSummaryResponse> artworks) {
        return new RoomDetailResponse(room.getRoomId(), MasterMapper.toResponse(room.getMaster()), room.getNameEs(),
                room.getNameEn(), room.getDescriptionEs(), room.getDescriptionEn(), room.getScene3dUrl(), artworks);
    }

    /** Copies the request fields into the entity. The master is set by the service. */
    public static void apply(RoomRequest request, Room room) {
        room.setNameEs(request.nameEs());
        room.setNameEn(request.nameEn());
        room.setDescriptionEs(request.descriptionEs());
        room.setDescriptionEn(request.descriptionEn());
        room.setScene3dUrl(request.scene3dUrl());
    }
}
