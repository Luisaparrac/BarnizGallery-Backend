package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.model.entity.Interaction;

/**
 * Spring Data repository for {@link Interaction}.
 */
public interface InteractionRepository extends JpaRepository<Interaction, Integer> {
}
