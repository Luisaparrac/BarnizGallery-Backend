package com.barnizgallery.backend.patterns.templatemethod;

import java.util.List;

import com.barnizgallery.backend.entity.OrderItem;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.enums.FulfillmentStatus;
import com.barnizgallery.backend.enums.OrderStatus;
import com.barnizgallery.backend.exception.BusinessRuleException;

/**
 * <b>Template Method pattern – Abstract class.</b>
 * <p>
 * Every shipping step of an order line follows the same algorithm, fixed in the final method {@link #apply}:
 * <ol>
 * <li>the line must be in the step's starting status (409 otherwise);</li>
 * <li>the step records its own data ({@link #record}, a hook that does nothing by default);</li>
 * <li>the line moves to the step's target status;</li>
 * <li>the order moves to the status the step derives from all its lines (or stays).</li>
 * </ol>
 * Subclasses only say what changes: {@link ShipStep} and {@link DeliverStep}.
 */
public abstract class FulfillmentStep {

    /** The template method: the order of the steps cannot be changed by subclasses. */
    public final void apply(OrderItem line, List<OrderItem> orderLines) {
        if (line.getFulfillmentStatus() != from()) {
            throw new BusinessRuleException(ErrorCode.FULFILLMENT_STEP_INVALID, line.getFulfillmentStatus());
        }
        record(line);
        line.setFulfillmentStatus(to());
        OrderStatus next = orderStatusAfter(orderLines);
        if (next != null) {
            line.getOrder().setStatus(next);
        }
    }

    protected abstract FulfillmentStatus from();

    protected abstract FulfillmentStatus to();

    /** Hook: data the step saves on the line before it moves. */
    protected void record(OrderItem line) {
    }

    /** The new status of the order once this line moved, or null to keep the current one. */
    protected abstract OrderStatus orderStatusAfter(List<OrderItem> orderLines);
}
