package com.barnizgallery.backend.patterns.templatemethod;

import java.util.List;

import com.barnizgallery.backend.entity.OrderItem;
import com.barnizgallery.backend.enums.FulfillmentStatus;
import com.barnizgallery.backend.enums.OrderStatus;

/**
 * <b>Template Method – Concrete class:</b> a pending line is shipped with its carrier and tracking number. The
 * order is in preparation while some line is still pending, and shipped when none is.
 */
public class ShipStep extends FulfillmentStep {

    private final String carrier;
    private final String trackingNumber;

    public ShipStep(String carrier, String trackingNumber) {
        this.carrier = carrier.trim();
        this.trackingNumber = trackingNumber.trim();
    }

    @Override
    protected FulfillmentStatus from() {
        return FulfillmentStatus.PENDING;
    }

    @Override
    protected FulfillmentStatus to() {
        return FulfillmentStatus.SHIPPED;
    }

    @Override
    protected void record(OrderItem line) {
        line.setCarrier(carrier);
        line.setTrackingNumber(trackingNumber);
    }

    @Override
    protected OrderStatus orderStatusAfter(List<OrderItem> orderLines) {
        boolean allShipped = orderLines.stream().allMatch(l -> l.getFulfillmentStatus() != FulfillmentStatus.PENDING);
        return allShipped ? OrderStatus.SHIPPED : OrderStatus.IN_PREPARATION;
    }
}
