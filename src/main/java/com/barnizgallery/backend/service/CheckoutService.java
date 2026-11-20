package com.barnizgallery.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.CheckoutRequest;
import com.barnizgallery.backend.dto.OrderResponse;
import com.barnizgallery.backend.entity.CartItem;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.mapper.AddressMapper;
import com.barnizgallery.backend.mapper.OrderMapper;
import com.barnizgallery.backend.patterns.builder.BuiltOrder;
import com.barnizgallery.backend.patterns.builder.OrderBuilder;
import com.barnizgallery.backend.repository.CartItemRepository;
import com.barnizgallery.backend.repository.OrderItemRepository;
import com.barnizgallery.backend.repository.OrderRepository;
import com.barnizgallery.backend.repository.PaymentObligationRepository;
import com.barnizgallery.backend.repository.UserRepository;
import com.barnizgallery.backend.security.CurrentUser;

import tools.jackson.databind.json.JsonMapper;

/**
 * Turns the cart into a {@code compra} order in one transaction: the order, one line per cart item and the single
 * payment obligation (built by {@link OrderBuilder}); then the cart is emptied. Every line must be for sale and in
 * the same currency, and every setting must be configured, before anything is written.
 */
@Service
public class CheckoutService {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final CartService cartService;
    private final AddressService addressService;
    private final CheckoutSettings checkoutSettings;
    private final OrderCodeGenerator codeGenerator;
    private final OrderRepository orderRepository;
    private final OrderItemRepository itemRepository;
    private final PaymentObligationRepository obligationRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;

    public CheckoutService(CartService cartService, AddressService addressService, CheckoutSettings checkoutSettings,
            OrderCodeGenerator codeGenerator, OrderRepository orderRepository, OrderItemRepository itemRepository,
            PaymentObligationRepository obligationRepository, CartItemRepository cartItemRepository,
            UserRepository userRepository, CurrentUser currentUser) {
        this.cartService = cartService;
        this.addressService = addressService;
        this.checkoutSettings = checkoutSettings;
        this.codeGenerator = codeGenerator;
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.obligationRepository = obligationRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.currentUser = currentUser;
    }

    @Transactional
    public OrderResponse checkout(CheckoutRequest request) {
        List<CartItem> lines = cartService.items();
        String currency = requireSingleCurrency(lines);
        OrderBuilder builder = new OrderBuilder()
                .commissionRate(checkoutSettings.commissionRate())
                .shipping(checkoutSettings.shipping(currency))
                .shippingAddress(JSON.writeValueAsString(
                        AddressMapper.toResponse(addressService.myAddress(request.addressId()))))
                .code(codeGenerator.next())
                .buyer(userRepository.getReferenceById(currentUser.requireId()))
                .currency(currency);
        lines.forEach(line -> builder.artwork(line.getArtwork(), line.getQuantity()));
        BuiltOrder built = builder.build();
        orderRepository.save(built.order());
        itemRepository.saveAll(built.items());
        obligationRepository.saveAll(built.obligations());
        cartItemRepository.deleteAll(lines);
        return OrderMapper.toResponse(built.order(), built.items(), built.obligations());
    }

    /** 409 when the cart is empty, a line cannot be bought or the lines mix currencies. */
    private static String requireSingleCurrency(List<CartItem> lines) {
        if (lines.isEmpty()) {
            throw new BusinessRuleException(ErrorCode.CART_EMPTY);
        }
        for (CartItem line : lines) {
            PurchaseRules.requirePurchasable(line.getArtwork());
            if (line.getQuantity() > line.getArtwork().getStock()) {
                throw new BusinessRuleException(ErrorCode.NOT_ENOUGH_STOCK, line.getArtwork().getStock());
            }
        }
        List<String> currencies = lines.stream().map(line -> line.getArtwork().getCurrency()).distinct().toList();
        if (currencies.size() > 1) {
            throw new BusinessRuleException(ErrorCode.CART_MIXED_CURRENCIES, String.join(", ", currencies));
        }
        return currencies.get(0);
    }
}
