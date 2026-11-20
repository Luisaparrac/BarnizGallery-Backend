package com.barnizgallery.backend.service;

import org.springframework.stereotype.Component;

import com.barnizgallery.backend.entity.Master;
import com.barnizgallery.backend.entity.Store;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.enums.MasterVerificationStatus;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.repository.MasterRepository;
import com.barnizgallery.backend.repository.StoreRepository;
import com.barnizgallery.backend.security.CurrentUser;

/**
 * The master and store of the logged-in artisan, only when the master is approved and active (403 otherwise).
 */
@Component
public class ApprovedArtisan {

    private final MasterRepository masterRepository;
    private final StoreRepository storeRepository;
    private final CurrentUser currentUser;

    public ApprovedArtisan(MasterRepository masterRepository, StoreRepository storeRepository, CurrentUser currentUser) {
        this.masterRepository = masterRepository;
        this.storeRepository = storeRepository;
        this.currentUser = currentUser;
    }

    public Master master() {
        Long userId = currentUser.requireId();
        Master master = masterRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ARTISAN_PROFILE_NOT_FOUND, userId));
        if (!master.isActive() || master.getVerificationStatus() != MasterVerificationStatus.APPROVED) {
            throw BusinessRuleException.forbidden(ErrorCode.FORBIDDEN);
        }
        return master;
    }

    public Store store(Master master) {
        return storeRepository.findFirstByMasterMasterIdOrderByStoreIdAsc(master.getMasterId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ARTISAN_STORE_NOT_FOUND, master.getMasterId()));
    }
}
