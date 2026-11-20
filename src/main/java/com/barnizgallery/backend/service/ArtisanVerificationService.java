package com.barnizgallery.backend.service;

import java.time.Clock;
import java.util.EnumSet;
import java.util.Set;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.barnizgallery.backend.dto.ArtisanVerificationResponse;
import com.barnizgallery.backend.dto.VerificationStepResponse;
import com.barnizgallery.backend.entity.Master;
import com.barnizgallery.backend.entity.MasterVerification;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.enums.MasterVerificationStatus;
import com.barnizgallery.backend.enums.VerificationStep;
import com.barnizgallery.backend.enums.VerificationStepStatus;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.exception.FeatureDisabledException;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.mapper.VerificationMapper;
import com.barnizgallery.backend.patterns.adapter.StorageService;
import com.barnizgallery.backend.patterns.adapter.StoredImage;
import com.barnizgallery.backend.repository.MasterRepository;
import com.barnizgallery.backend.repository.MasterVerificationRepository;
import com.barnizgallery.backend.security.CurrentUser;

/**
 * The verification steps of a master artisan, from the artisan's side.
 */
@Service
public class ArtisanVerificationService {

    /** Steps whose evidence the artisan sends; email and admin approval are decided by others. */
    static final Set<VerificationStep> EVIDENCE_STEPS = EnumSet.of(VerificationStep.IDENTITY,
            VerificationStep.WORKSHOP, VerificationStep.SAMPLES);

    private final MasterRepository masterRepository;
    private final MasterVerificationRepository verificationRepository;
    private final CurrentUser currentUser;
    private final StorageService storageService;
    private final ImageUploadValidator imageValidator;
    private final Clock clock;

    public ArtisanVerificationService(MasterRepository masterRepository,
            MasterVerificationRepository verificationRepository, CurrentUser currentUser,
            StorageService storageService, ImageUploadValidator imageValidator, Clock clock) {
        this.masterRepository = masterRepository;
        this.verificationRepository = verificationRepository;
        this.currentUser = currentUser;
        this.storageService = storageService;
        this.imageValidator = imageValidator;
        this.clock = clock;
    }

    /** Overall status and steps of the logged-in artisan. */
    @Transactional(readOnly = true)
    public ArtisanVerificationResponse myVerification() {
        Master master = myMaster();
        return VerificationMapper.toResponse(master,
                verificationRepository.findByMasterMasterIdOrderByVerificationIdAsc(master.getMasterId()));
    }

    /**
     * Records the evidence (a link) of an identity, workshop or samples step: the step becomes {@code enviado}
     * and a pending master goes {@code en_revision}. An approved step cannot change (409).
     */
    @Transactional
    public VerificationStepResponse submitEvidence(VerificationStep step, String evidenceUrl) {
        if (!EVIDENCE_STEPS.contains(step)) {
            throw BusinessRuleException.unprocessable(ErrorCode.STEP_WITHOUT_EVIDENCE, step.getDbValue());
        }
        Master master = myMaster();
        MasterVerification verification = verificationRepository
                .findByMasterMasterIdAndStep(master.getMasterId(), step)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.VERIFICATION_STEP_NOT_FOUND,
                        step.getDbValue()));
        if (verification.getStatus() == VerificationStepStatus.APPROVED) {
            throw new BusinessRuleException(ErrorCode.STEP_ALREADY_APPROVED, step.getDbValue());
        }
        verification.setEvidenceUrl(evidenceUrl);
        verification.setStatus(VerificationStepStatus.SUBMITTED);
        if (master.getVerificationStatus() == MasterVerificationStatus.PENDING
                || master.getVerificationStatus() == MasterVerificationStatus.REJECTED) {
            master.setVerificationStatus(MasterVerificationStatus.IN_REVIEW);
        }
        return VerificationMapper.toResponse(verification);
    }

    /** Uploads the evidence image to the storage (503 while it is not configured) and records its link. */
    @Transactional
    public VerificationStepResponse uploadEvidence(VerificationStep step, MultipartFile file) {
        if (!storageService.isEnabled()) {
            throw new FeatureDisabledException(ErrorCode.STORAGE_DISABLED);
        }
        Master master = myMaster();
        imageValidator.validate(file);
        StoredImage image = storageService.upload(file, "verifications/" + master.getMasterId());
        return submitEvidence(step, image.url());
    }

    /** The master profile of the logged-in artisan (404 when the account has none). */
    Master myMaster() {
        Long userId = currentUser.requireId();
        return masterRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ARTISAN_PROFILE_NOT_FOUND, userId));
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
