package com.barnizgallery.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.CartResponse;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Cart;
import com.barnizgallery.backend.entity.CartItem;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.mapper.CartMapper;
import com.barnizgallery.backend.repository.CartItemRepository;
import com.barnizgallery.backend.repository.CartRepository;
import com.barnizgallery.backend.repository.UserRepository;
import com.barnizgallery.backend.security.CurrentUser;

/**
 * The cart of the logged-in user. The cart is created with its first item; reading never writes.
 */
@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository itemRepository;
    private final CurrentUser currentUser;
    private final UserRepository userRepository;
    private final ArtworkService artworkService;

    public CartService(CartRepository cartRepository, CartItemRepository itemRepository, CurrentUser currentUser,
            UserRepository userRepository, ArtworkService artworkService) {
        this.userRepository = userRepository;
        this.artworkService = artworkService;
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

    /**
     * Puts an artwork in the cart with the given quantity (replacing the previous one). The artwork must be for
     * sale and have that many units now; nothing is reserved until checkout.
     */
    @Transactional
    public CartResponse put(Long artworkId, int quantity) {
        Artwork artwork = artworkService.getArtwork(artworkId);
        PurchaseRules.requirePurchasable(artwork);
        if (quantity > artwork.getStock()) {
            throw new BusinessRuleException(ErrorCode.NOT_ENOUGH_STOCK, artwork.getStock());
        }
        Long userId = currentUser.requireId();
        Cart cart = cartRepository.findByUserUserId(userId).orElseGet(() -> newCart(userId));
        CartItem item = itemRepository.findByCartCartIdAndArtworkArtworkId(cart.getCartId(), artworkId)
                .orElseGet(() -> newItem(cart, artwork));
        item.setQuantity(quantity);
        itemRepository.save(item);
        return view();
    }

    /** Adds units to the line of the artwork (creating it when missing); same rules as {@link #put}. */
    @Transactional
    public CartResponse add(Long artworkId, int quantity) {
        int current = cartRepository.findByUserUserId(currentUser.requireId())
                .flatMap(cart -> itemRepository.findByCartCartIdAndArtworkArtworkId(cart.getCartId(), artworkId))
                .map(CartItem::getQuantity)
                .orElse(0);
        return put(artworkId, current + quantity);
    }

    /** Removes an artwork from the cart (nothing happens when it is not there). */
    @Transactional
    public CartResponse remove(Long artworkId) {
        cartRepository.findByUserUserId(currentUser.requireId())
                .flatMap(cart -> itemRepository.findByCartCartIdAndArtworkArtworkId(cart.getCartId(), artworkId))
                .ifPresent(itemRepository::delete);
        return view();
    }

    private Cart newCart(Long userId) {
        Cart cart = new Cart();
        cart.setUser(userRepository.getReferenceById(userId));
        return cartRepository.save(cart);
    }

    private static CartItem newItem(Cart cart, Artwork artwork) {
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setArtwork(artwork);
        return item;
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
