package com.barnizgallery.backend.mapper;

import com.barnizgallery.backend.dto.IdentifyUserRequest;
import com.barnizgallery.backend.dto.TasteProfileRequest;
import com.barnizgallery.backend.dto.TasteProfileResponse;
import com.barnizgallery.backend.dto.UserResponse;
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
        return new TasteProfileResponse(profile.getProfileId(), profile.getUser().getUserId(),
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
