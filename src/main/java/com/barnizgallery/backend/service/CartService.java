package com.barnizgallery.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.CartResponse;
import com.barnizgallery.backend.entity.CartItem;
import com.barnizgallery.backend.mapper.CartMapper;
import com.barnizgallery.backend.repository.CartItemRepository;
import com.barnizgallery.backend.repository.CartRepository;
import com.barnizgallery.backend.security.CurrentUser;

/**
 * The cart of the logged-in user. The cart is created with its first item; reading never writes.
 */
@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository itemRepository;
    private final CurrentUser currentUser;

    public CartService(CartRepository cartRepository, CartItemRepository itemRepository, CurrentUser currentUser) {
        this.cartRepository = cartRepository;
        this.itemRepository = itemRepository;
        this.currentUser = currentUser;
    }

    @Transactional(readOnly = true)
    public CartResponse view() {
        return CartMapper.toResponse(items().stream()
                .map(item -> CartMapper.toResponse(item, isAvailable(item)))
                .toList());
    }

    /** The lines of the user's cart (empty when there is no cart yet). */
    List<CartItem> items() {
        return cartRepository.findByUserUserId(currentUser.requireId())
                .map(cart -> itemRepository.findByCartCartIdOrderByCartItemIdAsc(cart.getCartId()))
                .orElse(List.of());
    }

    static boolean isAvailable(CartItem item) {
        return PurchaseRules.isPurchasable(item.getArtwork()) && item.getQuantity() <= item.getArtwork().getStock();
    }
}
