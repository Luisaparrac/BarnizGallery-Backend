package com.barnizgallery.backend.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.TasteProfileRequest;
import com.barnizgallery.backend.dto.TasteProfileResponse;
import com.barnizgallery.backend.dto.UpdateProfileRequest;
import com.barnizgallery.backend.dto.UserPreferencesRequest;
import com.barnizgallery.backend.dto.UserResponse;
import com.barnizgallery.backend.entity.TasteProfile;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.mapper.UserMapper;
import com.barnizgallery.backend.repository.TasteProfileRepository;
import com.barnizgallery.backend.repository.UserRepository;
import com.barnizgallery.backend.security.CurrentUser;

/**
 * Users are identified only by their email (no password).
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final TasteProfileRepository tasteProfileRepository;
    private final CatalogCodeResolver catalogResolver;
    private final CurrentUser currentUser;

    public UserService(UserRepository userRepository, TasteProfileRepository tasteProfileRepository,
            CatalogCodeResolver catalogResolver, CurrentUser currentUser) {
        this.userRepository = userRepository;
        this.tasteProfileRepository = tasteProfileRepository;
        this.catalogResolver = catalogResolver;
        this.currentUser = currentUser;
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long userId) {
        return UserMapper.toResponse(getUser(userId));
    }

    /** Profile of the logged-in user. */
    @Transactional(readOnly = true)
    public UserResponse me() {
        return UserMapper.toResponse(getUser(currentUser.requireId()));
    }

    /**
     * Changes the logged-in user's profile. Null fields stay as they are; an empty avatar code removes the avatar.
     * A new language reaches the access token at the next refresh.
     */
    @Transactional
    public UserResponse updateMe(UpdateProfileRequest request) {
        User user = getUser(currentUser.requireId());
        if (request.name() != null) {
            user.setName(request.name().trim());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone().isBlank() ? null : request.phone().trim());
        }
        if (request.countryCode() != null) {
            user.setCountryCode(request.countryCode());
        }
        if (request.preferredLanguage() != null) {
            user.setPreferredLanguage(request.preferredLanguage());
        }
        if (request.avatarCode() != null) {
            user.setAvatar(catalogResolver.avatar(request.avatarCode()));
        }
        return UserMapper.toResponse(user);
    }

    /** Taste profile of the logged-in user. */
    @Transactional(readOnly = true)
    public TasteProfileResponse myTasteProfile() {
        return getTasteProfile(currentUser.requireId());
    }

    /** Creates or replaces the taste profile of the logged-in user (active catalog codes). */
    @Transactional
    public TasteProfileResponse saveMyTasteProfile(TasteProfileRequest request) {
        return saveTasteProfile(currentUser.requireId(), request);
    }

    /** Changes the preferred language. A null value is left as it is. */
    @Transactional
    public UserResponse updatePreferences(Long userId, UserPreferencesRequest request) {
        User user = getUser(userId);
        if (request.preferredLanguage() != null) {
            user.setPreferredLanguage(request.preferredLanguage());
        }
        return UserMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public TasteProfileResponse getTasteProfile(Long userId) {
        getUser(userId);
        return tasteProfileRepository.findByUserUserId(userId)
                .map(UserMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.TASTE_PROFILE_NOT_FOUND, userId));
    }

    /** Creates the taste profile or replaces its answers (catalog codes), and refreshes updated_at. */
    @Transactional
    public TasteProfileResponse saveTasteProfile(Long userId, TasteProfileRequest request) {
        User user = getUser(userId);
        TasteProfile profile = tasteProfileRepository.findByUserUserId(userId).orElseGet(() -> {
            TasteProfile created = new TasteProfile();
            created.setUser(user);
            return created;
        });
        profile.setColors(catalogResolver.colors(request.colorCodes()));
        profile.setShapes(catalogResolver.shapes(request.shapeCodes()));
        profile.setStyles(catalogResolver.styles(request.styleCodes()));
        profile.setMotifs(catalogResolver.motifs(request.motifCodes()));
        profile.setBudgetRange(catalogResolver.budgetRange(request.budgetRangeCode()));
        profile.setUpdatedAt(LocalDateTime.now());
        return UserMapper.toResponse(tasteProfileRepository.save(profile));
    }

    /** Loads a user or throws 404. Used by other services too. */
    @Transactional(readOnly = true)
    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, userId));
    }
}
