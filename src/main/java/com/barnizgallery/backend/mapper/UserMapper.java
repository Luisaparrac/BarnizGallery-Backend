package com.barnizgallery.backend.mapper;

import java.util.List;

import com.barnizgallery.backend.dto.TasteProfileResponse;
import com.barnizgallery.backend.dto.UserResponse;
import com.barnizgallery.backend.entity.Color;
import com.barnizgallery.backend.entity.Motif;
import com.barnizgallery.backend.entity.Shape;
import com.barnizgallery.backend.entity.Style;
import com.barnizgallery.backend.entity.TasteProfile;
import com.barnizgallery.backend.entity.User;

/**
 * Converts between {@link User} / {@link TasteProfile} and their DTOs.
 */
public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(user.getUserId(), user.getName(), user.getEmail(), user.getRole(), user.getStatus(),
                user.getCountryCode(), user.getPreferredLanguage(),
                user.getAvatar() == null ? null : user.getAvatar().getCode(), user.getUserId(), user.getName(),
                user.getAvatar() == null ? null : user.getAvatar().getAvatarId(), user.getPhone(),
                user.getEmailVerifiedAt() != null, user.getCreatedAt(), user.getLastLoginAt());
    }

    public static TasteProfileResponse toResponse(TasteProfile profile) {
        String budgetRangeCode = profile.getBudgetRange() == null ? null : profile.getBudgetRange().getCode();
        return new TasteProfileResponse(profile.getProfileId(), profile.getUser().getUserId(), colorCodes(profile),
                profile.getShapes().stream().map(Shape::getCode).sorted().toList(),
                profile.getStyles().stream().map(Style::getCode).sorted().toList(),
                profile.getMotifs().stream().map(Motif::getCode).sorted().toList(),
                budgetRangeCode, profile.getUpdatedAt(),
                profile.getColors().stream().map(Color::getColorId).sorted().toList(),
                profile.getShapes().stream().map(Shape::getShapeId).sorted().toList(),
                profile.getStyles().stream().map(Style::getStyleId).sorted().toList(),
                profile.getMotifs().stream().map(Motif::getMotifId).sorted().toList(),
                profile.getBudgetRange() == null ? null : profile.getBudgetRange().getBudgetRangeId());
    }

    /** Preferred color codes of the profile, sorted. */
    public static List<String> colorCodes(TasteProfile profile) {
        return profile.getColors().stream().map(Color::getCode).sorted().toList();
    }

}
