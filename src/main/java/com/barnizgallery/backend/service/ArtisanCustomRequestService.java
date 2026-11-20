package com.barnizgallery.backend.service;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.CustomRequestResponse;
import com.barnizgallery.backend.entity.CustomRequest;
import com.barnizgallery.backend.entity.Master;
import com.barnizgallery.backend.enums.CustomRequestStatus;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.repository.CustomRequestRepository;

/**
 * Custom requests seen by an approved artisan: the open ones (open to all or addressed to them, not expired), the
 * ones where their offer was accepted, and declining a request. The database has no table for declines, so a
 * decline is not stored: the customer of a request addressed to the artisan is notified with the reason, and the
 * declined list is empty.
 */
@Service
public class ArtisanCustomRequestService {

    static final String CUSTOM_REQUEST_DECLINED = "custom_request_declined";

    private final CustomRequestRepository requestRepository;
    private final CustomRequestService requestService;
    private final ApprovedArtisan approvedArtisan;
    private final NotificationService notificationService;
    private final Clock clock;

    public ArtisanCustomRequestService(CustomRequestRepository requestRepository, CustomRequestService requestService,
            ApprovedArtisan approvedArtisan, NotificationService notificationService, Clock clock) {
        this.requestRepository = requestRepository;
        this.requestService = requestService;
        this.approvedArtisan = approvedArtisan;
        this.notificationService = notificationService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<CustomRequestResponse> open() {
        Master master = approvedArtisan.master();
        return requestRepository.findByStatusOrderByCustomRequestIdDesc(CustomRequestStatus.OPEN).stream()
                .filter(r -> r.getTargetMaster() == null || r.getTargetMaster().getMasterId().equals(master.getMasterId()))
                .filter(r -> r.getExpiresAt() == null || r.getExpiresAt().isAfter(clock.instant()))
                .map(requestService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CustomRequestResponse> accepted() {
        Master master = approvedArtisan.master();
        return requestRepository.findByAcceptedOfferMasterMasterIdOrderByCustomRequestIdDesc(master.getMasterId())
                .stream().map(requestService::toResponse).toList();
    }

    /** Declines are not stored (no table yet), so this list is always empty. */
    @Transactional(readOnly = true)
    public List<CustomRequestResponse> declined() {
        approvedArtisan.master();
        return List.of();
    }

    @Transactional
    public void decline(Long requestId, String reason) {
        Master master = approvedArtisan.master();
        CustomRequest request = requestRepository.findById(requestId)
                .filter(r -> r.getStatus() == CustomRequestStatus.OPEN)
                .filter(r -> r.getTargetMaster() == null || r.getTargetMaster().getMasterId().equals(master.getMasterId()))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CUSTOM_REQUEST_NOT_FOUND, requestId));
        if (request.getTargetMaster() != null) {
            notificationService.notify(request.getBuyer(), CUSTOM_REQUEST_DECLINED, "/custom-requests/" + requestId,
                    null, master.getName(), reason == null || reason.isBlank() ? "-" : reason.strip());
        }
    }
}
