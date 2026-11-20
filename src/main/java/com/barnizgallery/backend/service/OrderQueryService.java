package com.barnizgallery.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.OrderResponse;
import com.barnizgallery.backend.dto.OrderStatusChangeResponse;
import com.barnizgallery.backend.entity.Order;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.mapper.OrderMapper;
import com.barnizgallery.backend.repository.OrderItemRepository;
import com.barnizgallery.backend.repository.OrderRepository;
import com.barnizgallery.backend.repository.OrderStatusHistoryRepository;
import com.barnizgallery.backend.repository.PaymentObligationRepository;
import com.barnizgallery.backend.security.CurrentUser;

/**
 * Orders of the logged-in buyer. An order of another buyer is answered as not found.
 */
@Service
public class OrderQueryService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository itemRepository;
    private final PaymentObligationRepository obligationRepository;
    private final CurrentUser currentUser;
    private final OrderStatusHistoryRepository historyRepository;

    public OrderQueryService(OrderRepository orderRepository, OrderItemRepository itemRepository,
            PaymentObligationRepository obligationRepository, CurrentUser currentUser,
            OrderStatusHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.obligationRepository = obligationRepository;
        this.currentUser = currentUser;
    }

    /** The buyer's orders, newest first, complete (lines, obligations and history) as the frontend reads them. */
    @Transactional(readOnly = true)
    public List<OrderResponse> mine() {
        return orderRepository.findByBuyerUserIdOrderByOrderIdDesc(currentUser.requireId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse mine(Long orderId) {
        return toResponse(myOrder(orderId));
    }

    private OrderResponse toResponse(Order order) {
        Long orderId = order.getOrderId();
        return OrderMapper.toResponse(order, itemRepository.findByOrderOrderIdOrderByOrderItemIdAsc(orderId),
                obligationRepository.findByOrderOrderIdOrderByObligationIdAsc(orderId),
                historyRepository.findByOrderOrderIdOrderByChangedAtAscHistoryIdAsc(orderId).stream()
                        .map(OrderMapper::toResponse).toList());
    }

    /**
     * Status changes of an own order, oldest first. The database writes them when the status changes (the
     * creation as {@code pendiente_pago} is not a change).
     */
    @Transactional(readOnly = true)
    public List<OrderStatusChangeResponse> history(Long orderId) {
        myOrder(orderId);
        return historyRepository.findByOrderOrderIdOrderByChangedAtAscHistoryIdAsc(orderId).stream()
                .map(OrderMapper::toResponse)
                .toList();
    }

    /** An order of the logged-in buyer (404 otherwise). */
    @Transactional(readOnly = true)
    public Order myOrder(Long orderId) {
        Long buyerId = currentUser.requireId();
        return orderRepository.findById(orderId)
                .filter(order -> order.getBuyer().getUserId().equals(buyerId))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND, orderId));
    }
}
