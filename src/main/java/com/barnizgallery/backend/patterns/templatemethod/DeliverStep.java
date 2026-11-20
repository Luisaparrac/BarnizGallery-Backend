package com.barnizgallery.backend.patterns.templatemethod;

import java.util.List;

import com.barnizgallery.backend.entity.OrderItem;
import com.barnizgallery.backend.enums.FulfillmentStatus;
import com.barnizgallery.backend.enums.OrderStatus;

/**
 * <b>Template Method – Concrete class:</b> a shipped line is delivered; the order is delivered with its last line.
 */
public class DeliverStep extends FulfillmentStep {

    @Override
    protected FulfillmentStatus from() {
        return FulfillmentStatus.SHIPPED;
    }

    @Override
    protected FulfillmentStatus to() {
        return FulfillmentStatus.DELIVERED;
    }

    @Override
    protected OrderStatus orderStatusAfter(List<OrderItem> orderLines) {
        boolean allDelivered = orderLines.stream().allMatch(l -> l.getFulfillmentStatus() == FulfillmentStatus.DELIVERED);
        return allDelivered ? OrderStatus.DELIVERED : null;
    }
}
