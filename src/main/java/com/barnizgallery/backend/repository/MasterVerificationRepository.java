package com.barnizgallery.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.MasterVerification;
import com.barnizgallery.backend.enums.VerificationStep;

/**
 * Spring Data repository for {@link MasterVerification}.
 */
public interface MasterVerificationRepository extends JpaRepository<MasterVerification, Long> {

    List<MasterVerification> findByMasterMasterIdOrderByVerificationIdAsc(Long masterId);

    Optional<MasterVerification> findByMasterMasterIdAndStep(Long masterId, VerificationStep step);
}
