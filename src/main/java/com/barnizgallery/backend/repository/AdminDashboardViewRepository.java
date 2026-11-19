package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.AdminDashboardView;

/**
 * Spring Data repository for {@link AdminDashboardView}.
 */
public interface AdminDashboardViewRepository extends JpaRepository<AdminDashboardView, Long> {
}
