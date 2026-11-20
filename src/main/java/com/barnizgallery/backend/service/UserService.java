package com.barnizgallery.backend.service;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.TasteProfileRequest;
import com.barnizgallery.backend.dto.TasteProfileResponse;
import com.barnizgallery.backend.dto.UpdateProfileRequest;
import com.barnizgallery.backend.dto.UserPreferencesRequest;
import com.barnizgallery.backend.dto.UserResponse;
import com.barnizgallery.backend.entity.TasteProfile;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.CatalogKind;
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
        String name = request.name() != null ? request.name() : request.fullName();
        if (name != null) {
            user.setName(name.trim());
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
        } else if (request.avatarId() != null) {
            user.setAvatar(catalogResolver.avatar(
                    catalogResolver.codesOf(CatalogKind.AVATARS, List.of(request.avatarId())).get(0)));
        }
        return UserMapper.toResponse(user);
    }

    /** Taste profile of the logged-in user, or null when they have not answered the questionnaire yet. */
    @Transactional(readOnly = true)
    public TasteProfileResponse myTasteProfile() {
        return tasteProfileRepository.findByUserUserId(currentUser.requireId()).map(UserMapper::toResponse).orElse(null);
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
        profile.setColors(catalogResolver.colors(merge(request.colorCodes(),
                catalogResolver.codesOf(CatalogKind.COLORS, request.colorIds()))));
        profile.setShapes(catalogResolver.shapes(merge(request.shapeCodes(),
                catalogResolver.codesOf(CatalogKind.SHAPES, request.shapeIds()))));
        profile.setStyles(catalogResolver.styles(merge(request.styleCodes(),
                catalogResolver.codesOf(CatalogKind.STYLES, request.styleIds()))));
        profile.setMotifs(catalogResolver.motifs(merge(request.motifCodes(),
                catalogResolver.codesOf(CatalogKind.MOTIFS, request.motifIds()))));
        profile.setBudgetRange(catalogResolver.budgetRange(request.budgetRangeCode() != null
                ? request.budgetRangeCode() : catalogResolver.budgetRangeCodeOf(request.budgetRangeId())));
        profile.setUpdatedAt(LocalDateTime.now());
        return UserMapper.toResponse(tasteProfileRepository.save(profile));
    }

    /** Codes given directly plus the codes of the given ids, without repeats. */
    private static List<String> merge(List<String> codes, List<String> codesOfIds) {
        Set<String> all = new LinkedHashSet<>();
        if (codes != null) {
            all.addAll(codes);
        }
        all.addAll(codesOfIds);
        return List.copyOf(all);
    }

    /** Loads a user or throws 404. Used by other services too. */
    @Transactional(readOnly = true)
    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, userId));
    }
}
