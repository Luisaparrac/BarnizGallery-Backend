package com.barnizgallery.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.Visitor;

/**
 * Spring Data repository for {@link Visitor}.
 */
public interface VisitorRepository extends JpaRepository<Visitor, Integer> {

    Optional<Visitor> findByEmailIgnoreCase(String email);
}
