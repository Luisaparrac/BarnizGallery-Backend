package com.barnizgallery.backend.mapper;

import com.barnizgallery.backend.dto.request.MasterRequest;
import com.barnizgallery.backend.dto.response.MasterDetailResponse;
import com.barnizgallery.backend.dto.response.MasterResponse;
import com.barnizgallery.backend.dto.response.RoomSummaryResponse;
import com.barnizgallery.backend.model.entity.Master;

/**
 * Converts between {@link Master} and its DTOs.
 */
public final class MasterMapper {

    private MasterMapper() {
    }

    public static MasterResponse toResponse(Master master) {
        return new MasterResponse(master.getMasterId(), master.getName(), master.getBiographyEs(),
                master.getBiographyEn(), master.getWorkshop(), master.getContactInfo());
    }

    public static MasterDetailResponse toDetail(Master master, RoomSummaryResponse room) {
        return new MasterDetailResponse(master.getMasterId(), master.getName(), master.getBiographyEs(),
                master.getBiographyEn(), master.getWorkshop(), master.getContactInfo(), room);
    }

    /** Copies the request fields into the entity (used for create and update). */
    public static void apply(MasterRequest request, Master master) {
        master.setName(request.name());
        master.setBiographyEs(request.biographyEs());
        master.setBiographyEn(request.biographyEn());
        master.setWorkshop(request.workshop());
        master.setContactInfo(request.contactInfo());
    }
}
