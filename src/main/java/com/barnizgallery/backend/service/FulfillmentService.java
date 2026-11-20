package com.barnizgallery.backend.service;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.ArtisanOrderLineResponse;
import com.barnizgallery.backend.dto.ShipmentRequest;
import com.barnizgallery.backend.entity.Order;
import com.barnizgallery.backend.entity.OrderItem;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.enums.FulfillmentStatus;
import com.barnizgallery.backend.enums.OrderStatus;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.mapper.OrderMapper;
import com.barnizgallery.backend.repository.OrderItemRepository;

/**
 * Shipping of the paid order lines of the logged-in artisan's store. Unpaid orders are not shown.
 */
@Service
public class FulfillmentService {

    static final Set<OrderStatus> TO_FULFIL = EnumSet.of(OrderStatus.PAID, OrderStatus.IN_PREPARATION,
            OrderStatus.SHIPPED, OrderStatus.DELIVERED);

    private final OrderItemRepository itemRepository;
    static final String ORDER_SHIPPED = "order_shipped";

    private final StoreEditorService editorService;
    private final NotificationService notificationService;

    public FulfillmentService(OrderItemRepository itemRepository, StoreEditorService editorService,
            NotificationService notificationService) {
        this.itemRepository = itemRepository;
        this.editorService = editorService;
        this.notificationService = notificationService;
    }

    /**
     * Marks a paid line as shipped with its carrier and tracking number. The order goes to {@code en_preparacion}
     * with its first shipped line and to {@code enviado} when every line is shipped; the buyer is notified.
     */
    @Transactional
    public ArtisanOrderLineResponse ship(Long orderItemId, ShipmentRequest request) {
        OrderItem item = myLine(orderItemId);
        if (item.getFulfillmentStatus() != FulfillmentStatus.PENDING) {
            throw new BusinessRuleException(ErrorCode.FULFILLMENT_STEP_INVALID, item.getFulfillmentStatus());
        }
        item.setFulfillmentStatus(FulfillmentStatus.SHIPPED);
        item.setCarrier(request.carrier().trim());
        item.setTrackingNumber(request.trackingNumber().trim());
        Order order = item.getOrder();
        boolean allShipped = lines(order).stream().allMatch(line -> line.getFulfillmentStatus() != FulfillmentStatus.PENDING);
        order.setStatus(allShipped ? OrderStatus.SHIPPED : OrderStatus.IN_PREPARATION);
        notificationService.notify(order.getBuyer(), ORDER_SHIPPED, "/orders/" + order.getOrderId(), null,
                order.getOrderCode(), item.getCarrier(), item.getTrackingNumber());
        return OrderMapper.toArtisanLine(item);
    }

    /** A paid line of the artisan's store (404 otherwise). */
    OrderItem myLine(Long orderItemId) {
        Long storeId = editorService.myStore().getStoreId();
        return itemRepository.findById(orderItemId)
                .filter(item -> item.getStore().getStoreId().equals(storeId))
                .filter(item -> TO_FULFIL.contains(item.getOrder().getStatus()))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_ITEM_NOT_FOUND, orderItemId));
    }

    List<OrderItem> lines(Order order) {
        return itemRepository.findByOrderOrderIdOrderByOrderItemIdAsc(order.getOrderId());
    }

    /** Paid lines of the store (paid, in preparation, shipped or delivered orders), newest order first. */
    @Transactional(readOnly = true)
    public List<ArtisanOrderLineResponse> lines() {
        return itemRepository.findByStoreAndOrderStatus(editorService.myStore().getStoreId(), TO_FULFIL).stream()
                .map(OrderMapper::toArtisanLine)
                .toList();
    }
}
