package com.barnizgallery.backend.mapper;

import com.barnizgallery.backend.dto.MasterDetailResponse;
import com.barnizgallery.backend.dto.MasterRequest;
import com.barnizgallery.backend.dto.MasterResponse;
import com.barnizgallery.backend.dto.StoreSummaryResponse;
import com.barnizgallery.backend.entity.Master;

/**
 * Converts between {@link Master} and its DTOs.
 */
public final class MasterMapper {

    private MasterMapper() {
    }

    public static MasterResponse toResponse(Master master) {
        return new MasterResponse(master.getMasterId(), master.getName(), master.getBiographyEs(),
                master.getBiographyEn(), master.getWorkshop(), master.getContactInfo(),
                master.getVerificationStatus(), master.isActive());
    }

    public static MasterDetailResponse toDetail(Master master, StoreSummaryResponse store) {
        return new MasterDetailResponse(master.getMasterId(), master.getName(), master.getBiographyEs(),
                master.getBiographyEn(), master.getWorkshop(), master.getContactInfo(),
                master.getVerificationStatus(), master.isActive(), store);
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
