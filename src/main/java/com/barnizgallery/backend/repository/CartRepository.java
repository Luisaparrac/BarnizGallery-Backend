package com.barnizgallery.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.Cart;

/**
 * Spring Data repository for {@link Cart}.
 */
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserUserId(Long userId);
}
