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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.dto.IdentifyVisitorRequest;
import com.barnizgallery.backend.dto.IdentifyVisitorResponse;
import com.barnizgallery.backend.dto.TasteProfileRequest;
import com.barnizgallery.backend.dto.TasteProfileResponse;
import com.barnizgallery.backend.dto.VisitorPreferencesRequest;
import com.barnizgallery.backend.dto.VisitorResponse;
import com.barnizgallery.backend.entity.TasteProfile;
import com.barnizgallery.backend.entity.Visitor;
import com.barnizgallery.backend.enums.CameraMode;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.repository.TasteProfileRepository;
import com.barnizgallery.backend.repository.VisitorRepository;

@ExtendWith(MockitoExtension.class)
class VisitorServiceTest {

    @Mock
    private VisitorRepository visitorRepository;
    @Mock
    private TasteProfileRepository tasteProfileRepository;
    @InjectMocks
    private VisitorService visitorService;

    private final IdentifyVisitorRequest request = new IdentifyVisitorRequest("  Ana@Mail.COM ", "Ana", "Colombia",
            Language.ES, CameraMode.THIRD_PERSON);

    @Test
    void identifyReturnsExistingVisitorWithoutCreatingIt() {
        Visitor existing = TestEntities.visitor(4, Language.EN);
        when(visitorRepository.findByEmailIgnoreCase("ana@mail.com")).thenReturn(Optional.of(existing));

        IdentifyVisitorResponse response = visitorService.identify(request);

        assertThat(response.isNew()).isFalse();
        assertThat(response.visitor().visitorId()).isEqualTo(4);
        verify(visitorRepository, never()).save(any());
    }

    @Test
    void identifyCreatesVisitorWithNormalizedEmail() {
        when(visitorRepository.findByEmailIgnoreCase("ana@mail.com")).thenReturn(Optional.empty());
        when(visitorRepository.save(any(Visitor.class))).thenAnswer(inv -> {
            Visitor saved = inv.getArgument(0);
            saved.setVisitorId(9);
            return saved;
        });

        IdentifyVisitorResponse response = visitorService.identify(request);

        assertThat(response.isNew()).isTrue();
        assertThat(response.visitor().email()).isEqualTo("ana@mail.com");
        assertThat(response.visitor().cameraMode()).isEqualTo(CameraMode.THIRD_PERSON);
    }

    @Test
    void updatePreferencesOnlyChangesGivenFields() {
        Visitor visitor = TestEntities.visitor(1, Language.ES);
        when(visitorRepository.findById(1)).thenReturn(Optional.of(visitor));

        VisitorResponse updated = visitorService.updatePreferences(1, new VisitorPreferencesRequest(Language.EN, null));

        assertThat(updated.preferredLanguage()).isEqualTo(Language.EN);
        assertThat(updated.cameraMode()).isEqualTo(CameraMode.FIRST_PERSON);
    }

    @Test
    void tasteProfileMissingIs404() {
        when(visitorRepository.findById(1)).thenReturn(Optional.of(TestEntities.visitor(1, Language.ES)));
        when(tasteProfileRepository.findByVisitorVisitorId(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> visitorService.getTasteProfile(1)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void saveTasteProfileUpdatesExistingProfileAndTimestamp() {
        Visitor visitor = TestEntities.visitor(1, Language.ES);
        TasteProfile profile = new TasteProfile();
        profile.setVisitor(visitor);
        profile.setUpdatedAt(LocalDateTime.of(2020, 1, 1, 0, 0));
        when(visitorRepository.findById(1)).thenReturn(Optional.of(visitor));
        when(tasteProfileRepository.findByVisitorVisitorId(1)).thenReturn(Optional.of(profile));
        when(tasteProfileRepository.save(profile)).thenReturn(profile);

        TasteProfileResponse saved = visitorService.saveTasteProfile(1,
                new TasteProfileRequest(List.of("rojo"), null, null, "low"));

        assertThat(saved.preferredColors()).containsExactly("rojo");
        assertThat(saved.updatedAt()).isAfter(LocalDateTime.of(2020, 1, 1, 0, 0));
    }

    @Test
    void unknownVisitorIs404() {
        when(visitorRepository.findById(77)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> visitorService.findById(77)).hasMessage("Visitor 77 not found");
    }
}
