package com.barnizgallery.backend.patterns.templatemethod;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.barnizgallery.backend.entity.Order;
import com.barnizgallery.backend.entity.OrderItem;
import com.barnizgallery.backend.enums.FulfillmentStatus;
import com.barnizgallery.backend.enums.OrderStatus;

class FulfillmentStepTest {

    private final Order order = new Order();
    private final OrderItem first = line();
    private final OrderItem second = line();
    private final List<OrderItem> lines = List.of(first, second);

    @Test
    void shippingFollowsTheTemplateAndMovesTheOrder() {
        order.setStatus(OrderStatus.PAID);

        new ShipStep(" Servientrega ", "SV-1").apply(first, lines);
        assertThat(first.getCarrier()).isEqualTo("Servientrega");
        assertThat(first.getFulfillmentStatus()).isEqualTo(FulfillmentStatus.SHIPPED);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PREPARING);

        new ShipStep("Servientrega", "SV-2").apply(second, lines);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);

        new DeliverStep().apply(first, lines);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        new DeliverStep().apply(second, lines);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    void aStepOnlyStartsFromItsOwnStatus() {
        assertThatThrownBy(() -> new DeliverStep().apply(first, lines)).hasMessage("fulfillment_step_invalid [PENDING]");
    }

    private OrderItem line() {
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setFulfillmentStatus(FulfillmentStatus.PENDING);
        return item;
    }
}
