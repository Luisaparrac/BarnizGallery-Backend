package com.barnizgallery.backend.service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.IdentifyUserRequest;
import com.barnizgallery.backend.dto.IdentifyUserResponse;
import com.barnizgallery.backend.dto.TasteProfileRequest;
import com.barnizgallery.backend.dto.TasteProfileResponse;
import com.barnizgallery.backend.dto.UserPreferencesRequest;
import com.barnizgallery.backend.dto.UserResponse;
import com.barnizgallery.backend.entity.TasteProfile;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.mapper.UserMapper;
import com.barnizgallery.backend.repository.TasteProfileRepository;
import com.barnizgallery.backend.repository.UserRepository;

/**
 * Users are identified only by their email (no password).
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final TasteProfileRepository tasteProfileRepository;
    private final CatalogCodeResolver catalogResolver;

    public UserService(UserRepository userRepository, TasteProfileRepository tasteProfileRepository,
            CatalogCodeResolver catalogResolver) {
        this.userRepository = userRepository;
        this.tasteProfileRepository = tasteProfileRepository;
        this.catalogResolver = catalogResolver;
    }

    /** Finds the user by email; if it does not exist, creates it. */
    @Transactional
    public IdentifyUserResponse identify(IdentifyUserRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        Optional<User> existing = userRepository.findByEmailIgnoreCase(email);
        if (existing.isPresent()) {
            return new IdentifyUserResponse(UserMapper.toResponse(existing.get()), false);
        }
        User created = userRepository.save(UserMapper.toNewUser(request, email));
        return new IdentifyUserResponse(UserMapper.toResponse(created), true);
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long userId) {
        return UserMapper.toResponse(getUser(userId));
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
