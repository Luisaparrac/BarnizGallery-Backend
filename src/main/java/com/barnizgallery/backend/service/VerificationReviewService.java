package com.barnizgallery.backend.service;

import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.ArtisanVerificationResponse;
import com.barnizgallery.backend.dto.StepReviewRequest;
import com.barnizgallery.backend.dto.VerificationStepResponse;
import com.barnizgallery.backend.entity.MasterVerification;
import com.barnizgallery.backend.entity.PendingVerificationView;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.enums.VerificationStepStatus;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.mapper.VerificationMapper;
import com.barnizgallery.backend.repository.MasterVerificationRepository;
import com.barnizgallery.backend.repository.PendingVerificationViewRepository;
import com.barnizgallery.backend.repository.UserRepository;
import com.barnizgallery.backend.security.CurrentUser;

/**
 * The administrator's side of the artisan verification.
 */
@Service
public class VerificationReviewService {

    public static final String STEP_APPROVED = "verification_step_approved";
    public static final String STEP_REJECTED = "verification_step_rejected";

    private final PendingVerificationViewRepository pendingRepository;
    private final MasterVerificationRepository verificationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final CurrentUser currentUser;
    private final Clock clock;

    public VerificationReviewService(PendingVerificationViewRepository pendingRepository,
            MasterVerificationRepository verificationRepository, UserRepository userRepository,
            NotificationService notificationService, CurrentUser currentUser, Clock clock) {
        this.pendingRepository = pendingRepository;
        this.verificationRepository = verificationRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.currentUser = currentUser;
        this.clock = clock;
    }

    /**
     * Approves or rejects one step (identity, workshop or samples; the email step approves itself and the final
     * approval has its own action). The artisan is notified with the notes; a rejected step can be sent again.
     */
    @Transactional
    public VerificationStepResponse review(Long verificationId, StepReviewRequest request) {
        if (request.decision() != VerificationStepStatus.APPROVED
                && request.decision() != VerificationStepStatus.REJECTED) {
            throw BusinessRuleException.unprocessable(ErrorCode.REVIEW_DECISION_INVALID);
        }
        MasterVerification step = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.VERIFICATION_STEP_NOT_FOUND, verificationId));
        if (!ArtisanVerificationService.EVIDENCE_STEPS.contains(step.getStep())) {
            throw BusinessRuleException.unprocessable(ErrorCode.STEP_WITHOUT_EVIDENCE, step.getStep().getDbValue());
        }
        step.setStatus(request.decision());
        step.setNotes(request.notes());
        step.setReviewedBy(userRepository.getReferenceById(currentUser.requireId()));
        step.setReviewedAt(clock.instant());

        User artisan = step.getMaster().getUser();
        if (artisan != null) {
            String type = request.decision() == VerificationStepStatus.APPROVED ? STEP_APPROVED : STEP_REJECTED;
            notificationService.notify(artisan, type, "/artisan/verification",
                    "{\"step\":\"" + step.getStep().name() + "\"}", request.notes() == null ? "" : request.notes());
        }
        return VerificationMapper.toResponse(step);
    }

    /**
     * Masters waiting for a decision, from the view {@code v_pending_verifications}: masters that are pending or
     * in review, and steps that are pending, submitted or in review. One entry per master with its steps.
     */
    @Transactional(readOnly = true)
    public List<ArtisanVerificationResponse> pending() {
        Map<Long, List<PendingVerificationView>> byMaster = new LinkedHashMap<>();
        for (PendingVerificationView row : pendingRepository.findAllByOrderByMasterIdAscVerificationIdAsc()) {
            byMaster.computeIfAbsent(row.getMasterId(), id -> new ArrayList<>()).add(row);
        }
        List<ArtisanVerificationResponse> result = new ArrayList<>();
        byMaster.forEach((masterId, rows) -> {
            PendingVerificationView first = rows.get(0);
            List<VerificationStepResponse> steps = rows.stream()
                    .filter(row -> row.getVerificationId() != null)
                    .map(row -> new VerificationStepResponse(row.getVerificationId(), row.getStep(),
                            row.getStepStatus(), row.getEvidenceUrl(), row.getNotes(), null))
                    .toList();
            result.add(new ArtisanVerificationResponse(masterId, first.getName(), first.getVerificationStatus(),
                    steps));
        });
        return result;
    }
}
