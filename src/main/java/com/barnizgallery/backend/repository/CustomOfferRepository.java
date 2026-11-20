package com.barnizgallery.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.CustomOffer;

/**
 * Spring Data repository for {@link CustomOffer}.
 */
public interface CustomOfferRepository extends JpaRepository<CustomOffer, Long> {

    List<CustomOffer> findByCustomRequestCustomRequestIdOrderByCustomOfferIdAsc(Long customRequestId);
}
