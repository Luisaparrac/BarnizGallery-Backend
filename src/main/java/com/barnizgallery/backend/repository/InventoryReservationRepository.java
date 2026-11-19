package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.InventoryReservation;

/**
 * Spring Data repository for {@link InventoryReservation}.
 */
public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {
}
