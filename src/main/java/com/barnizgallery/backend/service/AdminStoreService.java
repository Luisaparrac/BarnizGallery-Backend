package com.barnizgallery.backend.service;

import java.time.Clock;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.AdminStoreResponse;
import com.barnizgallery.backend.entity.Master;
import com.barnizgallery.backend.entity.Store;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.enums.MasterVerificationStatus;
import com.barnizgallery.backend.enums.StoreStatus;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.exception.DatabaseErrors;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.repository.StoreRepository;

/**
 * Administrators list every store and publish the store of an active, approved master, giving it a slug when it has
 * none. The database checks the master again when the store is published.
 */
@Service
public class AdminStoreService {

    private final StoreRepository storeRepository;
    private final Clock clock;

    public AdminStoreService(StoreRepository storeRepository, Clock clock) {
        this.storeRepository = storeRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<AdminStoreResponse> findAll() {
        return storeRepository.findAllWithMaster().stream().map(AdminStoreService::toResponse).toList();
    }

    @Transactional
    public AdminStoreResponse publish(Long storeId, String slug) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.STORE_NOT_FOUND, storeId));
        Master master = store.getMaster();
        ActiveMasterRule.requireActive(master);
        if (master.getVerificationStatus() != MasterVerificationStatus.APPROVED) {
            throw new BusinessRuleException(ErrorCode.STORE_MASTER_NOT_APPROVED);
        }
        if (slug != null && !slug.equals(store.getSlug())) {
            if (storeRepository.existsBySlug(slug)) {
                throw new BusinessRuleException(ErrorCode.SLUG_TAKEN, slug);
            }
            store.setSlug(slug);
        }
        if (store.getSlug() == null) {
            throw BusinessRuleException.unprocessable(ErrorCode.STORE_SLUG_REQUIRED);
        }
        if (store.getStatus() != StoreStatus.PUBLISHED) {
            store.setStatus(StoreStatus.PUBLISHED);
            store.setPublishedAt(clock.instant());
        }
        try {
            storeRepository.saveAndFlush(store);
        } catch (DataAccessException ex) {
            if (DatabaseErrors.isRuleViolation(ex)) {
                throw new BusinessRuleException(ErrorCode.STORE_MASTER_NOT_APPROVED);
            }
            throw ex;
        }
        return toResponse(store);
    }

    private static AdminStoreResponse toResponse(Store store) {
        Master master = store.getMaster();
        return new AdminStoreResponse(store.getStoreId(), store.getSlug(), store.getNameEs(), store.getNameEn(),
                store.getStatus(), store.getPublishedAt(), master.getMasterId(), master.getName(),
                master.getVerificationStatus(), master.isActive());
    }
}
