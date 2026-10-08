package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.model.entity.Visitor;

/**
 * Spring Data repository for {@link Visitor}.
 */
public interface VisitorRepository extends JpaRepository<Visitor, Integer> {
}
