package com.barnizgallery.backend.service;

import java.time.Clock;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.entity.MasterVerification;
import com.barnizgallery.backend.enums.VerificationStep;
import com.barnizgallery.backend.enums.VerificationStepStatus;
import com.barnizgallery.backend.repository.MasterRepository;
import com.barnizgallery.backend.repository.MasterVerificationRepository;

/**
 * The verification steps of a master artisan, from the artisan's side.
 */
@Service
public class ArtisanVerificationService {

    private final MasterRepository masterRepository;
    private final MasterVerificationRepository verificationRepository;
    private final Clock clock;

    public ArtisanVerificationService(MasterRepository masterRepository,
            MasterVerificationRepository verificationRepository, Clock clock) {
        this.masterRepository = masterRepository;
        this.verificationRepository = verificationRepository;
        this.clock = clock;
    }

    /** The email step is approved by the system as soon as the artisan confirms the email. */
    @EventListener
    @Transactional
    public void onEmailVerified(EmailVerified event) {
        masterRepository.findByUserUserId(event.user().getUserId())
                .flatMap(master -> verificationRepository.findByMasterMasterIdAndStep(master.getMasterId(),
                        VerificationStep.EMAIL))
                .filter(step -> step.getStatus() != VerificationStepStatus.APPROVED)
                .ifPresent(this::approveEmailStep);
    }

    private void approveEmailStep(MasterVerification step) {
        step.setStatus(VerificationStepStatus.APPROVED);
        step.setReviewedAt(clock.instant());
    }
}
