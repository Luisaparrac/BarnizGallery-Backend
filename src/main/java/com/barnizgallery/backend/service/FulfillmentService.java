package com.barnizgallery.backend.service;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.ArtisanOrderLineResponse;
import com.barnizgallery.backend.enums.OrderStatus;
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
    private final StoreEditorService editorService;

    public FulfillmentService(OrderItemRepository itemRepository, StoreEditorService editorService) {
        this.itemRepository = itemRepository;
        this.editorService = editorService;
    }

    /** Paid lines of the store (paid, in preparation, shipped or delivered orders), newest order first. */
    @Transactional(readOnly = true)
    public List<ArtisanOrderLineResponse> lines() {
        return itemRepository.findByStoreAndOrderStatus(editorService.myStore().getStoreId(), TO_FULFIL).stream()
                .map(OrderMapper::toArtisanLine)
                .toList();
    }
}
