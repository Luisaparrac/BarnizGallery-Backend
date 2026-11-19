package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.CustomOffer;

/**
 * Spring Data repository for {@link CustomOffer}.
 */
public interface CustomOfferRepository extends JpaRepository<CustomOffer, Long> {
}
