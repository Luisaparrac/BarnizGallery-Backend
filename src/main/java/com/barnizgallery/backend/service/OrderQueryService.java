package com.barnizgallery.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.OrderResponse;
import com.barnizgallery.backend.dto.OrderSummaryResponse;
import com.barnizgallery.backend.entity.Order;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.mapper.OrderMapper;
import com.barnizgallery.backend.repository.OrderItemRepository;
import com.barnizgallery.backend.repository.OrderRepository;
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

    public OrderQueryService(OrderRepository orderRepository, OrderItemRepository itemRepository,
            PaymentObligationRepository obligationRepository, CurrentUser currentUser) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.obligationRepository = obligationRepository;
        this.currentUser = currentUser;
    }

    /** The buyer's orders, newest first. */
    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> mine() {
        return orderRepository.findByBuyerUserIdOrderByOrderIdDesc(currentUser.requireId()).stream()
                .map(OrderMapper::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse mine(Long orderId) {
        Order order = myOrder(orderId);
        return OrderMapper.toResponse(order, itemRepository.findByOrderOrderIdOrderByOrderItemIdAsc(orderId),
                obligationRepository.findByOrderOrderIdOrderByObligationIdAsc(orderId));
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
