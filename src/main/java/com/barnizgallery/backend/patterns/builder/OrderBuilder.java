package com.barnizgallery.backend.patterns.builder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Order;
import com.barnizgallery.backend.entity.OrderItem;
import com.barnizgallery.backend.entity.PaymentObligation;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.FulfillmentStatus;
import com.barnizgallery.backend.enums.OrderStatus;
import com.barnizgallery.backend.enums.OrderType;
import com.barnizgallery.backend.enums.PaymentObligationStage;
import com.barnizgallery.backend.enums.PaymentObligationStatus;

/**
 * <b>Builder pattern – Builder.</b>
 * <p>
 * Assembles a direct purchase: the order, one line per artwork and the single payment obligation. The amounts
 * must match the database rules exactly, so they are computed in one place:
 * <ul>
 * <li>line total = quantity × unit price; platform fee = line total × commission rate, rounded half up to cents;
 * artisan net = line total − fee; artisan gross = fee + net (= line total).</li>
 * <li>subtotal = sum of line totals; total = subtotal + shipping; platform fee of the order = sum of line fees.</li>
 * <li>one {@code pago_unico} obligation for the whole total.</li>
 * </ul>
 * The database recomputes the order totals with its triggers and checks them; both results are the same.
 * <pre>
 * BuiltOrder built = new OrderBuilder()
 *         .code("MUS-261120-K7Q2XM").buyer(user).currency("USD")
 *         .commissionRate(new BigDecimal("0.10")).shipping(new BigDecimal("25"))
 *         .shippingAddress(addressJson).paymentDueAt(expiresAt)
 *         .artwork(artwork, 1)
 *         .build();
 * </pre>
 */
public class OrderBuilder {

    private String code;
    private User buyer;
    private String currency;
    private BigDecimal commissionRate;
    private BigDecimal shipping = BigDecimal.ZERO;
    private String shippingAddress;
    private Instant paymentDueAt;
    private final List<Artwork> artworks = new ArrayList<>();
    private final List<Integer> quantities = new ArrayList<>();

    public OrderBuilder code(String code) {
        this.code = code;
        return this;
    }

    public OrderBuilder buyer(User buyer) {
        this.buyer = buyer;
        return this;
    }

    public OrderBuilder currency(String currency) {
        this.currency = currency;
        return this;
    }

    /** Share of each line kept by the platform, between 0 (included) and 1 (excluded). */
    public OrderBuilder commissionRate(BigDecimal commissionRate) {
        this.commissionRate = commissionRate;
        return this;
    }

    public OrderBuilder shipping(BigDecimal shipping) {
        this.shipping = shipping;
        return this;
    }

    /** Copy of the shipping address as a JSON object. */
    public OrderBuilder shippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
        return this;
    }

    /** When the reservation ends and the order must be paid (optional). */
    public OrderBuilder paymentDueAt(Instant paymentDueAt) {
        this.paymentDueAt = paymentDueAt;
        return this;
    }

    /** Adds a line for an artwork at its current price; its currency must be the order currency. */
    public OrderBuilder artwork(Artwork artwork, int quantity) {
        artworks.add(artwork);
        quantities.add(quantity);
        return this;
    }

    /** Checks the parts and computes every amount. */
    public BuiltOrder build() {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(buyer, "buyer");
        Objects.requireNonNull(currency, "currency");
        Objects.requireNonNull(shippingAddress, "shippingAddress");
        if (commissionRate == null || commissionRate.signum() < 0 || commissionRate.compareTo(BigDecimal.ONE) >= 0) {
            throw new IllegalStateException("The commission rate must be between 0 and 1");
        }
        if (shipping == null || shipping.signum() < 0) {
            throw new IllegalStateException("The shipping cost cannot be negative");
        }
        if (artworks.isEmpty()) {
            throw new IllegalStateException("An order needs at least one line");
        }
        Order order = newOrder();
        List<OrderItem> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal fees = BigDecimal.ZERO;
        for (int i = 0; i < artworks.size(); i++) {
            OrderItem item = line(order, artworks.get(i), quantities.get(i));
            subtotal = subtotal.add(item.getLineTotal());
            fees = fees.add(item.getPlatformFeeAmount());
            items.add(item);
        }
        BigDecimal shippingCost = shipping.setScale(2, RoundingMode.HALF_UP);
        order.setSubtotal(subtotal);
        order.setShippingCost(shippingCost);
        order.setPlatformFee(fees);
        order.setTotal(subtotal.add(shippingCost));
        return new BuiltOrder(order, items, List.of(singlePayment(order)));
    }

    private Order newOrder() {
        Order order = new Order();
        order.setOrderCode(code);
        order.setBuyer(buyer);
        order.setOrderType(OrderType.PURCHASE);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setCurrency(currency);
        order.setShippingAddress(shippingAddress);
        order.setPaymentDueAt(paymentDueAt);
        return order;
    }

    private OrderItem line(Order order, Artwork artwork, int quantity) {
        if (!currency.equals(artwork.getCurrency())) {
            throw new IllegalStateException("Artwork " + artwork.getArtworkId() + " is not priced in " + currency);
        }
        BigDecimal unitPrice = artwork.getPrice().setScale(2, RoundingMode.HALF_UP);
        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal fee = lineTotal.multiply(commissionRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal net = lineTotal.subtract(fee);
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setStore(artwork.getStore());
        item.setArtwork(artwork);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        item.setLineTotal(lineTotal);
        item.setCurrency(currency);
        item.setPlatformFeeAmount(fee);
        item.setArtisanNetAmount(net);
        item.setArtisanGrossAmount(fee.add(net));
        item.setFulfillmentStatus(FulfillmentStatus.PENDING);
        return item;
    }

    private PaymentObligation singlePayment(Order order) {
        PaymentObligation obligation = new PaymentObligation();
        obligation.setOrder(order);
        obligation.setStage(PaymentObligationStage.SINGLE_PAYMENT);
        obligation.setAmount(order.getTotal());
        obligation.setCurrency(currency);
        obligation.setStatus(PaymentObligationStatus.PENDING);
        obligation.setDueAt(paymentDueAt);
        return obligation;
    }
}
