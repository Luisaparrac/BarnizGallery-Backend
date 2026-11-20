package com.barnizgallery.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.CustomRequest;
import com.barnizgallery.backend.enums.CustomRequestStatus;

/**
 * Spring Data repository for {@link CustomRequest}.
 */
public interface CustomRequestRepository extends JpaRepository<CustomRequest, Long> {

    List<CustomRequest> findByBuyerUserIdOrderByCustomRequestIdDesc(Long buyerId);

    List<CustomRequest> findByStatusOrderByCustomRequestIdDesc(CustomRequestStatus status);

    List<CustomRequest> findByAcceptedOfferMasterMasterIdOrderByCustomRequestIdDesc(Long masterId);
}
