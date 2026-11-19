package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.Cart;

/**
 * Spring Data repository for {@link Cart}.
 */
public interface CartRepository extends JpaRepository<Cart, Long> {
}
