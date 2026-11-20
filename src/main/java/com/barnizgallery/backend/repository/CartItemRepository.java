package com.barnizgallery.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.CartItem;

/**
 * Spring Data repository for {@link CartItem}.
 */
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCartCartIdOrderByCartItemIdAsc(Long cartId);
}
