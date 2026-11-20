package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.unit.DataSize;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.dto.RegisterArtisanRequest;
import com.barnizgallery.backend.dto.RegisterRequest;
import com.barnizgallery.backend.dto.StepReviewRequest;
import com.barnizgallery.backend.entity.Master;
import com.barnizgallery.backend.entity.MasterVerification;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.enums.MasterVerificationStatus;
import com.barnizgallery.backend.enums.UserRole;
import com.barnizgallery.backend.enums.UserStatus;
import com.barnizgallery.backend.enums.VerificationStep;
import com.barnizgallery.backend.enums.VerificationStepStatus;
import com.barnizgallery.backend.patterns.adapter.DisabledStorageService;
import com.barnizgallery.backend.repository.MasterRepository;
import com.barnizgallery.backend.repository.MasterVerificationRepository;
import com.barnizgallery.backend.repository.PendingVerificationViewRepository;
import com.barnizgallery.backend.repository.UserRepository;
import com.barnizgallery.backend.security.CurrentUser;

/** Sign-up, evidence, review and approval of a master artisan, with repositories in memory (mocks). */
class ArtisanVerificationFlowTest {

    private static final Instant NOW = Instant.parse("2026-11-20T12:00:00Z");

    private final MasterRepository masters = mock(MasterRepository.class);
    private final MasterVerificationRepository steps = mock(MasterVerificationRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final NotificationService notifications = mock(NotificationService.class);
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
    private final ArtisanVerificationService artisanSide = new ArtisanVerificationService(masters, steps,
            new CurrentUser(), new DisabledStorageService(), new ImageUploadValidator(DataSize.ofMegabytes(5)), clock);
    private final VerificationReviewService adminSide = new VerificationReviewService(
            mock(PendingVerificationViewRepository.class), steps, masters, users, notifications, new CurrentUser(), clock);

    private final User artisan = TestEntities.user(7, Language.EN);
    private final Master master = TestEntities.master(70);
    private final List<MasterVerification> verification = new ArrayList<>();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void signUpCreatesTheMasterAndItsFiveSteps() {
        RegistrationService registration = mock(RegistrationService.class);
        when(registration.createAccount(any(), eq(UserRole.ARTISAN))).thenReturn(artisan);
        when(masters.save(any(Master.class))).thenAnswer(inv -> inv.getArgument(0));
        ArtisanRegistrationService service = new ArtisanRegistrationService(registration, masters, steps,
                notifications, mock(SessionIssuer.class));

        service.register(new RegisterArtisanRequest(new RegisterRequest("Ana Maestra", "ana@taller.test",
                "una-clave-segura", "CO", Language.ES, null, null, null, null, null), "Taller Ana", "Pasto", null, null, null));

        ArgumentCaptor<Master> saved = ArgumentCaptor.forClass(Master.class);
        verify(masters).save(saved.capture());
        assertThat(saved.getValue().getUser()).isSameAs(artisan);
        assertThat(saved.getValue().getVerificationStatus()).isEqualTo(MasterVerificationStatus.PENDING);
        ArgumentCaptor<MasterVerification> created = ArgumentCaptor.forClass(MasterVerification.class);
        verify(steps, times(5)).save(created.capture());
        assertThat(created.getAllValues()).extracting(MasterVerification::getStep)
                .containsExactly(VerificationStep.values());
        assertThat(created.getAllValues()).allSatisfy(s -> assertThat(s.getStatus())
                .isEqualTo(VerificationStepStatus.PENDING));
    }

    @Test
    void confirmingTheEmailApprovesTheEmailStep() {
        givenArtisanWithSteps();

        artisanSide.onEmailVerified(new EmailVerified(artisan));

        assertThat(step(VerificationStep.EMAIL).getStatus()).isEqualTo(VerificationStepStatus.APPROVED);
    }

    @Test
    void evidenceIsSentOnlyForEvidenceStepsAndNotAfterApproval() {
        givenArtisanWithSteps();
        login(7, "ARTISAN");

        artisanSide.submitEvidence(VerificationStep.IDENTITY, "https://docs.test/id.pdf");

        assertThat(step(VerificationStep.IDENTITY).getStatus()).isEqualTo(VerificationStepStatus.SUBMITTED);
        assertThat(master.getVerificationStatus()).isEqualTo(MasterVerificationStatus.IN_REVIEW);
        assertThatThrownBy(() -> artisanSide.submitEvidence(VerificationStep.ADMIN_APPROVAL, "https://x.test"))
                .hasMessage("step_without_evidence [aprobacion_admin]");
        step(VerificationStep.WORKSHOP).setStatus(VerificationStepStatus.APPROVED);
        assertThatThrownBy(() -> artisanSide.submitEvidence(VerificationStep.WORKSHOP, "https://x.test"))
                .hasMessage("step_already_approved [taller]");
        assertThatThrownBy(() -> artisanSide.uploadEvidence(VerificationStep.SAMPLES, null))
                .hasMessage("storage_disabled");
    }

    @Test
    void adminReviewsStepsAndApprovesOnlyWhenEverythingPassed() {
        givenArtisanWithSteps();
        login(1, "ADMIN");
        MasterVerification identity = step(VerificationStep.IDENTITY);
        when(steps.findById(identity.getVerificationId())).thenReturn(Optional.of(identity));

        adminSide.review(identity.getVerificationId(),
                new StepReviewRequest(VerificationStepStatus.REJECTED, "La foto no se lee"));

        assertThat(identity.getStatus()).isEqualTo(VerificationStepStatus.REJECTED);
        assertThat(identity.getNotes()).isEqualTo("La foto no se lee");
        verify(notifications).notify(eq(artisan), eq(VerificationReviewService.STEP_REJECTED), anyString(),
                anyString(), eq("La foto no se lee"));
        assertThatThrownBy(() -> adminSide.approveArtisan(70L))
                .hasMessage("verification_incomplete [EMAIL, IDENTITY, WORKSHOP, SAMPLES]");

        verification.stream().filter(s -> s.getStep() != VerificationStep.ADMIN_APPROVAL)
                .forEach(s -> s.setStatus(VerificationStepStatus.APPROVED));
        adminSide.approveArtisan(70L);

        assertThat(master.getVerificationStatus()).isEqualTo(MasterVerificationStatus.APPROVED);
        assertThat(master.getVerifiedAt()).isEqualTo(NOW);
        assertThat(step(VerificationStep.ADMIN_APPROVAL).getStatus()).isEqualTo(VerificationStepStatus.APPROVED);
        assertThat(artisan.getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(notifications).notify(artisan, VerificationReviewService.ARTISAN_APPROVED, "/artisan/store", null,
                master.getName());
    }

    @Test
    void onlyApprovedOrRejectedAreReviewDecisions() {
        assertThatThrownBy(() -> adminSide.review(1L, new StepReviewRequest(VerificationStepStatus.SUBMITTED, null)))
                .hasMessage("review_decision_invalid");
        verify(steps, never()).findById(any());
    }

    private void givenArtisanWithSteps() {
        artisan.setStatus(UserStatus.PENDING);
        artisan.setRole(UserRole.ARTISAN);
        master.setUser(artisan);
        long id = 100;
        for (VerificationStep value : VerificationStep.values()) {
            MasterVerification step = new MasterVerification();
            step.setVerificationId(id++);
            step.setMaster(master);
            step.setStep(value);
            step.setStatus(VerificationStepStatus.PENDING);
            verification.add(step);
            when(steps.findByMasterMasterIdAndStep(70L, value)).thenReturn(Optional.of(step));
        }
        when(masters.findByUserUserId(7L)).thenReturn(Optional.of(master));
        when(masters.findById(70L)).thenReturn(Optional.of(master));
        when(steps.findByMasterMasterIdOrderByVerificationIdAsc(70L)).thenReturn(verification);
        when(users.getReferenceById(1L)).thenReturn(TestEntities.user(1, Language.ES));
    }

    private MasterVerification step(VerificationStep value) {
        return verification.stream().filter(s -> s.getStep() == value).findFirst().orElseThrow();
    }

    private static void login(long userId, String role) {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "HS256").subject(String.valueOf(userId))
                .claim("role", role).issuedAt(NOW).expiresAt(NOW.plusSeconds(60)).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }
}
