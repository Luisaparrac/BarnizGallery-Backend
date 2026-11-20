package com.barnizgallery.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Component;

import com.barnizgallery.backend.entity.CustomOffer;
import com.barnizgallery.backend.entity.CustomRequest;
import com.barnizgallery.backend.entity.Order;
import com.barnizgallery.backend.entity.OrderItem;
import com.barnizgallery.backend.entity.PaymentObligation;
import com.barnizgallery.backend.enums.FulfillmentStatus;
import com.barnizgallery.backend.enums.OrderStatus;
import com.barnizgallery.backend.enums.OrderType;
import com.barnizgallery.backend.enums.PaymentObligationStage;
import com.barnizgallery.backend.enums.PaymentObligationStatus;
import com.barnizgallery.backend.repository.OrderItemRepository;
import com.barnizgallery.backend.repository.OrderRepository;
import com.barnizgallery.backend.repository.PaymentObligationRepository;

/**
 * Creates the {@code encargo} order of an accepted offer: one line for the offer, no shipping, and two payment
 * obligations: the deposit ({@code custom_orders.deposit_percent} of the total, rounded half up) and the balance.
 */
@Component
public class CustomOrderCreator {

    static final String DEPOSIT_PERCENT = "custom_orders.deposit_percent";

    private final OrderRepository orderRepository;
    private final OrderItemRepository itemRepository;
    private final PaymentObligationRepository obligationRepository;
    private final OrderCodeGenerator codeGenerator;
    private final CheckoutSettings checkoutSettings;
    private final AppSettingsService settings;

    public CustomOrderCreator(OrderRepository orderRepository, OrderItemRepository itemRepository,
            PaymentObligationRepository obligationRepository, OrderCodeGenerator codeGenerator,
            CheckoutSettings checkoutSettings, AppSettingsService settings) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.obligationRepository = obligationRepository;
        this.codeGenerator = codeGenerator;
        this.checkoutSettings = checkoutSettings;
        this.settings = settings;
    }

    public Order create(CustomRequest request, CustomOffer offer) {
        BigDecimal rate = checkoutSettings.commissionRate();
        BigDecimal percent = settings.decimal(DEPOSIT_PERCENT);
        BigDecimal total = offer.getPrice().setScale(2, RoundingMode.HALF_UP);
        BigDecimal fee = total.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        Order order = new Order();
        order.setOrderCode(codeGenerator.next());
        order.setBuyer(request.getBuyer());
        order.setOrderType(OrderType.CUSTOM);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setCurrency(offer.getCurrency());
        order.setSubtotal(total);
        order.setShippingCost(BigDecimal.ZERO.setScale(2));
        order.setPlatformFee(fee);
        order.setTotal(total);
        order.setShippingAddress("{}");
        orderRepository.save(order);
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setStore(offer.getStore());
        item.setCustomOffer(offer);
        item.setQuantity(1);
        item.setUnitPrice(total);
        item.setLineTotal(total);
        item.setCurrency(offer.getCurrency());
        item.setPlatformFeeAmount(fee);
        item.setArtisanNetAmount(total.subtract(fee));
        item.setArtisanGrossAmount(total);
        item.setFulfillmentStatus(FulfillmentStatus.PENDING);
        itemRepository.save(item);
        BigDecimal deposit = total.multiply(percent).setScale(2, RoundingMode.HALF_UP);
        obligationRepository.saveAll(List.of(obligation(order, PaymentObligationStage.DEPOSIT, deposit),
                obligation(order, PaymentObligationStage.BALANCE, total.subtract(deposit))));
        return order;
    }

    private static PaymentObligation obligation(Order order, PaymentObligationStage stage, BigDecimal amount) {
        PaymentObligation obligation = new PaymentObligation();
        obligation.setOrder(order);
        obligation.setStage(stage);
        obligation.setBasisAmount(order.getTotal());
        obligation.setBasisCurrency(order.getCurrency());
        obligation.setAmount(amount);
        obligation.setCurrency(order.getCurrency());
        obligation.setStatus(PaymentObligationStatus.PENDING);
        return obligation;
    }
}
