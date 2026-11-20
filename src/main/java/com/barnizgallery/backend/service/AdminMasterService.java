package com.barnizgallery.backend.service;

import java.time.Clock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.MasterResponse;
import com.barnizgallery.backend.entity.Master;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.enums.MasterVerificationStatus;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.mapper.MasterMapper;
import com.barnizgallery.backend.repository.MasterRepository;
import com.barnizgallery.backend.repository.MasterVerificationRepository;
import com.barnizgallery.backend.repository.UserRepository;
import com.barnizgallery.backend.security.CurrentUser;

/**
 * Administrators approve an active master. A master that signed up as an artisan has verification steps and goes
 * through {@link VerificationReviewService#approveArtisan} (all steps must pass); a master of the roster loaded
 * by the migrations has no steps and is approved directly. Archived masters cannot be approved (409).
 */
@Service
public class AdminMasterService {

    private final MasterRepository masterRepository;
    private final MasterVerificationRepository verificationRepository;
    private final VerificationReviewService reviewService;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;
    private final Clock clock;

    public AdminMasterService(MasterRepository masterRepository, MasterVerificationRepository verificationRepository,
            VerificationReviewService reviewService, UserRepository userRepository, CurrentUser currentUser,
            Clock clock) {
        this.masterRepository = masterRepository;
        this.verificationRepository = verificationRepository;
        this.reviewService = reviewService;
        this.userRepository = userRepository;
        this.currentUser = currentUser;
        this.clock = clock;
    }

    @Transactional
    public MasterResponse approve(Long masterId) {
        Master master = masterRepository.findById(masterId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MASTER_NOT_FOUND, masterId));
        ActiveMasterRule.requireActive(master);
        if (!verificationRepository.findByMasterMasterIdOrderByVerificationIdAsc(masterId).isEmpty()) {
            reviewService.approveArtisan(masterId);
            return MasterMapper.toResponse(master);
        }
        if (master.getVerificationStatus() != MasterVerificationStatus.APPROVED) {
            master.setVerificationStatus(MasterVerificationStatus.APPROVED);
            master.setVerifiedAt(clock.instant());
            master.setVerifiedBy(userRepository.getReferenceById(currentUser.requireId()));
        }
        return MasterMapper.toResponse(master);
    }
}
