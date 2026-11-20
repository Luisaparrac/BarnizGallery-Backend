package com.barnizgallery.backend.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.CheckoutRequest;
import com.barnizgallery.backend.dto.OrderResponse;
import com.barnizgallery.backend.entity.CartItem;
import com.barnizgallery.backend.entity.OrderItem;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.exception.DatabaseErrors;
import com.barnizgallery.backend.mapper.AddressMapper;
import com.barnizgallery.backend.mapper.OrderMapper;
import com.barnizgallery.backend.patterns.builder.BuiltOrder;
import com.barnizgallery.backend.patterns.builder.OrderBuilder;
import com.barnizgallery.backend.patterns.chain.CartNotEmptyCheck;
import com.barnizgallery.backend.patterns.chain.CheckoutCheck;
import com.barnizgallery.backend.patterns.chain.ForSaleCheck;
import com.barnizgallery.backend.patterns.chain.SingleCurrencyCheck;
import com.barnizgallery.backend.patterns.chain.StockCheck;
import com.barnizgallery.backend.repository.CartItemRepository;
import com.barnizgallery.backend.repository.OrderItemRepository;
import com.barnizgallery.backend.repository.OrderRepository;
import com.barnizgallery.backend.repository.PaymentObligationRepository;
import com.barnizgallery.backend.repository.UserRepository;
import com.barnizgallery.backend.security.CurrentUser;

import tools.jackson.databind.json.JsonMapper;

/**
 * Turns the cart into a {@code compra} order in one transaction: the order, one line per cart item and the single
 * payment obligation (built by {@link OrderBuilder}); each line is reserved with {@code museo.reserve_order_item}
 * until the payment deadline ({@code cart.reservation_minutes}); then the cart is emptied. Stock is never changed
 * here: the database consumes it when the payment is approved. Every line must be for sale and in
 * the same currency, and every setting must be configured, before anything is written.
 */
@Service
public class CheckoutService {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final CheckoutCheck CHECKS = checks();

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
    private final Clock clock;

    public CheckoutService(CartService cartService, AddressService addressService, CheckoutSettings checkoutSettings,
            OrderCodeGenerator codeGenerator, OrderRepository orderRepository, OrderItemRepository itemRepository,
            PaymentObligationRepository obligationRepository, CartItemRepository cartItemRepository,
            UserRepository userRepository, CurrentUser currentUser, Clock clock) {
        this.clock = clock;
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
        Instant reservedUntil = clock.instant().plus(Duration.ofMinutes(checkoutSettings.reservationMinutes()));
        OrderBuilder builder = new OrderBuilder()
                .paymentDueAt(reservedUntil)
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
        built.items().forEach(item -> reserve(item, reservedUntil));
        cartItemRepository.deleteAll(lines);
        return OrderMapper.toResponse(built.order(), built.items(), built.obligations());
    }

    /**
     * Reserves the line in the same transaction. The database refuses a missing stock or not enough units (also
     * counting other active reservations); that refusal rolls the whole checkout back and answers 409.
     */
    private void reserve(OrderItem item, Instant until) {
        try {
            itemRepository.reserve(item.getOrderItemId(), until);
        } catch (DataAccessException ex) {
            if (DatabaseErrors.isRuleViolation(ex)) {
                throw new BusinessRuleException(ErrorCode.RESERVATION_REJECTED, item.getArtwork().getArtworkId());
            }
            throw ex;
        }
    }

    /** 409 when the cart is empty, a line cannot be bought or the lines mix currencies (Chain of Responsibility). */
    private static String requireSingleCurrency(List<CartItem> lines) {
        CHECKS.handle(lines);
        return lines.get(0).getArtwork().getCurrency();
    }

    private static CheckoutCheck checks() {
        CheckoutCheck first = new CartNotEmptyCheck();
        first.then(new ForSaleCheck(PurchaseRules::isPurchasable))
                .then(new StockCheck())
                .then(new SingleCurrencyCheck());
        return first;
    }
}
