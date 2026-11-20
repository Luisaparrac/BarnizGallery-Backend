package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.dto.IdentifyUserRequest;
import com.barnizgallery.backend.dto.IdentifyUserResponse;
import com.barnizgallery.backend.dto.TasteProfileRequest;
import com.barnizgallery.backend.dto.TasteProfileResponse;
import com.barnizgallery.backend.dto.UserPreferencesRequest;
import com.barnizgallery.backend.dto.UserResponse;
import com.barnizgallery.backend.entity.TasteProfile;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.enums.UserRole;
import com.barnizgallery.backend.enums.UserStatus;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.repository.TasteProfileRepository;
import com.barnizgallery.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TasteProfileRepository tasteProfileRepository;
    @Mock
    private CatalogCodeResolver catalogResolver;
    @InjectMocks
    private UserService userService;

    private final IdentifyUserRequest request = new IdentifyUserRequest("  Ana@Mail.COM ", "Ana", "CO",
            Language.ES);

    @Test
    void identifyReturnsExistingUserWithoutCreatingIt() {
        User existing = TestEntities.user(4, Language.EN);
        when(userRepository.findByEmailIgnoreCase("ana@mail.com")).thenReturn(Optional.of(existing));

        IdentifyUserResponse response = userService.identify(request);

        assertThat(response.isNew()).isFalse();
        assertThat(response.user().userId()).isEqualTo(4);
        verify(userRepository, never()).save(any());
    }

    @Test
    void identifyCreatesUserWithNormalizedEmail() {
        when(userRepository.findByEmailIgnoreCase("ana@mail.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User saved = inv.getArgument(0);
            saved.setUserId(9L);
            return saved;
        });

        IdentifyUserResponse response = userService.identify(request);

        assertThat(response.isNew()).isTrue();
        assertThat(response.user().email()).isEqualTo("ana@mail.com");
        assertThat(response.user().role()).isEqualTo(UserRole.CUSTOMER);
        assertThat(response.user().status()).isEqualTo(UserStatus.PENDING);
        assertThat(response.user().countryCode()).isEqualTo("CO");
    }

    @Test
    void updatePreferencesChangesTheLanguage() {
        User user = TestEntities.user(1, Language.ES);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse updated = userService.updatePreferences(1L, new UserPreferencesRequest(Language.EN));

        assertThat(updated.preferredLanguage()).isEqualTo(Language.EN);
    }

    @Test
    void tasteProfileMissingIs404() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(TestEntities.user(1, Language.ES)));
        when(tasteProfileRepository.findByUserUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getTasteProfile(1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void saveTasteProfileUpdatesExistingProfileAndTimestamp() {
        User user = TestEntities.user(1, Language.ES);
        TasteProfile profile = new TasteProfile();
        profile.setUser(user);
        profile.setUpdatedAt(LocalDateTime.of(2020, 1, 1, 0, 0));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(tasteProfileRepository.findByUserUserId(1L)).thenReturn(Optional.of(profile));
        when(tasteProfileRepository.save(profile)).thenReturn(profile);
        when(catalogResolver.colors(List.of("rojo"))).thenReturn(Set.of(TestEntities.color("rojo")));

        TasteProfileResponse saved = userService.saveTasteProfile(1L,
                new TasteProfileRequest(List.of("rojo"), null, null, null, null));

        assertThat(saved.colorCodes()).containsExactly("rojo");
        assertThat(saved.budgetRangeCode()).isNull();
        assertThat(saved.updatedAt()).isAfter(LocalDateTime.of(2020, 1, 1, 0, 0));
    }

    @Test
    void unknownUserIs404() {
        when(userRepository.findById(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(77L)).hasMessage("user_not_found [77]");
    }
}
