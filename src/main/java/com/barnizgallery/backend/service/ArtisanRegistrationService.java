package com.barnizgallery.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.AuthResponse;
import com.barnizgallery.backend.dto.RegisterArtisanRequest;
import com.barnizgallery.backend.entity.Master;
import com.barnizgallery.backend.entity.MasterVerification;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.MasterVerificationStatus;
import com.barnizgallery.backend.enums.UserRole;
import com.barnizgallery.backend.enums.VerificationStep;
import com.barnizgallery.backend.enums.VerificationStepStatus;
import com.barnizgallery.backend.repository.MasterRepository;
import com.barnizgallery.backend.repository.MasterVerificationRepository;

/**
 * Sign-up of master artisans. In one transaction it creates the {@code artesano} account (pending), its master
 * profile ({@code pendiente}) and the five verification steps (email, identity, workshop, samples and admin
 * approval), all {@code pendiente}. The artisan can log in at once to follow the steps; a store can only be
 * published after an administrator approves the master.
 */
@Service
public class ArtisanRegistrationService {

    public static final String ARTISAN_WELCOME = "artisan_welcome";

    private final RegistrationService registrationService;
    private final MasterRepository masterRepository;
    private final MasterVerificationRepository verificationRepository;
    private final NotificationService notificationService;
    private final SessionIssuer sessionIssuer;

    public ArtisanRegistrationService(RegistrationService registrationService, MasterRepository masterRepository,
            MasterVerificationRepository verificationRepository, NotificationService notificationService,
            SessionIssuer sessionIssuer) {
        this.registrationService = registrationService;
        this.masterRepository = masterRepository;
        this.verificationRepository = verificationRepository;
        this.notificationService = notificationService;
        this.sessionIssuer = sessionIssuer;
    }

    @Transactional
    public AuthResponse register(RegisterArtisanRequest request) {
        User user = registrationService.createAccount(request.account(), UserRole.ARTISAN);
        Master master = new Master();
        master.setUser(user);
        master.setName(user.getName());
        master.setWorkshop(request.workshop());
        master.setCity(request.city());
        master.setPhone(request.phone());
        master.setBiographyEs(request.biographyEs());
        master.setBiographyEn(request.biographyEn());
        master.setVerificationStatus(MasterVerificationStatus.PENDING);
        Master saved = masterRepository.save(master);
        for (VerificationStep step : VerificationStep.values()) {
            MasterVerification verification = new MasterVerification();
            verification.setMaster(saved);
            verification.setStep(step);
            verification.setStatus(VerificationStepStatus.PENDING);
            verificationRepository.save(verification);
        }
        notificationService.notify(user, ARTISAN_WELCOME, "/artisan/verification", null, user.getName());
        return sessionIssuer.open(user);
    }
}
