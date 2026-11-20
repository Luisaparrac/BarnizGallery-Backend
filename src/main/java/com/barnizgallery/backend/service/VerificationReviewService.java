package com.barnizgallery.backend.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.ArtisanVerificationResponse;
import com.barnizgallery.backend.dto.VerificationStepResponse;
import com.barnizgallery.backend.entity.PendingVerificationView;
import com.barnizgallery.backend.repository.PendingVerificationViewRepository;

/**
 * The administrator's side of the artisan verification.
 */
@Service
public class VerificationReviewService {

    private final PendingVerificationViewRepository pendingRepository;

    public VerificationReviewService(PendingVerificationViewRepository pendingRepository) {
        this.pendingRepository = pendingRepository;
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
