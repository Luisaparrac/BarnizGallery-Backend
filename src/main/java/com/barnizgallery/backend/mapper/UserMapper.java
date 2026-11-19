package com.barnizgallery.backend.mapper;

import java.util.List;

import com.barnizgallery.backend.dto.IdentifyUserRequest;
import com.barnizgallery.backend.dto.TasteProfileResponse;
import com.barnizgallery.backend.dto.UserResponse;
import com.barnizgallery.backend.entity.Color;
import com.barnizgallery.backend.entity.Motif;
import com.barnizgallery.backend.entity.Shape;
import com.barnizgallery.backend.entity.Style;
import com.barnizgallery.backend.entity.TasteProfile;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.UserRole;
import com.barnizgallery.backend.enums.UserStatus;

/**
 * Converts between {@link User} / {@link TasteProfile} and their DTOs.
 */
public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(user.getUserId(), user.getName(), user.getEmail(), user.getRole(), user.getStatus(),
                user.getCountryCode(), user.getPreferredLanguage());
    }

    /**
     * Builds a new customer from the identification request. The email is normalized by the service.
     * The account stays pending and without password until it gets one.
     */
    public static User toNewUser(IdentifyUserRequest request, String normalizedEmail) {
        User user = new User();
        user.setEmail(normalizedEmail);
        user.setName(request.name());
        user.setCountryCode(request.countryCode());
        user.setPreferredLanguage(request.preferredLanguage());
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(UserStatus.PENDING);
        return user;
    }

    public static TasteProfileResponse toResponse(TasteProfile profile) {
        String budgetRangeCode = profile.getBudgetRange() == null ? null : profile.getBudgetRange().getCode();
        return new TasteProfileResponse(profile.getProfileId(), profile.getUser().getUserId(), colorCodes(profile),
                profile.getShapes().stream().map(Shape::getCode).sorted().toList(),
                profile.getStyles().stream().map(Style::getCode).sorted().toList(),
                profile.getMotifs().stream().map(Motif::getCode).sorted().toList(),
                budgetRangeCode, profile.getUpdatedAt());
    }

    /** Preferred color codes of the profile, sorted. */
    public static List<String> colorCodes(TasteProfile profile) {
        return profile.getColors().stream().map(Color::getCode).sorted().toList();
    }

}
