package com.barnizgallery.backend.mapper;

import com.barnizgallery.backend.dto.request.IdentifyVisitorRequest;
import com.barnizgallery.backend.dto.request.TasteProfileRequest;
import com.barnizgallery.backend.dto.response.TasteProfileResponse;
import com.barnizgallery.backend.dto.response.VisitorResponse;
import com.barnizgallery.backend.model.entity.TasteProfile;
import com.barnizgallery.backend.model.entity.Visitor;

/**
 * Converts between {@link Visitor} / {@link TasteProfile} and their DTOs.
 */
public final class VisitorMapper {

    private VisitorMapper() {
    }

    public static VisitorResponse toResponse(Visitor visitor) {
        return new VisitorResponse(visitor.getVisitorId(), visitor.getName(), visitor.getEmail(),
                visitor.getCountry(), visitor.getPreferredLanguage(), visitor.getCameraMode());
    }

    /** Builds a new visitor from the identification request. The email is normalized by the service. */
    public static Visitor toNewVisitor(IdentifyVisitorRequest request, String normalizedEmail) {
        Visitor visitor = new Visitor();
        visitor.setEmail(normalizedEmail);
        visitor.setName(request.name());
        visitor.setCountry(request.country());
        visitor.setPreferredLanguage(request.preferredLanguage());
        visitor.setCameraMode(request.cameraMode());
        return visitor;
    }

    public static TasteProfileResponse toResponse(TasteProfile profile) {
        return new TasteProfileResponse(profile.getProfileId(), profile.getVisitor().getVisitorId(),
                profile.getPreferredColors(), profile.getPreferredTypes(), profile.getPreferredStyles(),
                profile.getBudgetRange(), profile.getUpdatedAt());
    }

    /** Copies the questionnaire answers into the profile. */
    public static void apply(TasteProfileRequest request, TasteProfile profile) {
        profile.setPreferredColors(request.preferredColors());
        profile.setPreferredTypes(request.preferredTypes());
        profile.setPreferredStyles(request.preferredStyles());
        profile.setBudgetRange(request.budgetRange());
    }
}
