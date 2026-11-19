package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.CartItem;

/**
 * Spring Data repository for {@link CartItem}.
 */
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
