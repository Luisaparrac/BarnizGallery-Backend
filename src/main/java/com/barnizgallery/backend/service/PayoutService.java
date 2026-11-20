package com.barnizgallery.backend.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.PayoutResponse;
import com.barnizgallery.backend.entity.Master;
import com.barnizgallery.backend.entity.OrderItem;
import com.barnizgallery.backend.entity.Payout;
import com.barnizgallery.backend.entity.PayoutItem;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.enums.FulfillmentStatus;
import com.barnizgallery.backend.enums.PayoutStatus;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.mapper.PayoutMapper;
import com.barnizgallery.backend.repository.OrderItemRepository;
import com.barnizgallery.backend.repository.PayoutItemRepository;
import com.barnizgallery.backend.repository.PayoutRepository;

/**
 * Payouts to artisans (administrators). A payout groups the delivered lines of one master in one currency that
 * are not in a payout yet; each line contributes its artisan net amount. The database keeps the payout amount
 * equal to the sum of its items.
 */
@Service
public class PayoutService {

    private final PayoutRepository payoutRepository;
    private final PayoutItemRepository payoutItemRepository;
    static final String PAYOUT_PAID = "payout_paid";

    private final OrderItemRepository orderItemRepository;
    private final NotificationService notificationService;
    private final Clock clock;

    public PayoutService(PayoutRepository payoutRepository, PayoutItemRepository payoutItemRepository,
            OrderItemRepository orderItemRepository, NotificationService notificationService, Clock clock) {
        this.notificationService = notificationService;
        this.clock = clock;
        this.payoutRepository = payoutRepository;
        this.payoutItemRepository = payoutItemRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional(readOnly = true)
    public List<PayoutResponse> findAll() {
        return payoutRepository.findAllWithMaster().stream().map(PayoutMapper::toResponse).toList();
    }

    /** Marks a pending payout as paid with its transfer reference and tells the artisan. */
    @Transactional
    public PayoutResponse markPaid(Long payoutId, String reference) {
        Payout payout = payoutRepository.findById(payoutId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PAYOUT_NOT_FOUND, payoutId));
        if (payout.getStatus() != PayoutStatus.PENDING) {
            throw new BusinessRuleException(ErrorCode.PAYOUT_ALREADY_PAID, payoutId);
        }
        payout.setStatus(PayoutStatus.PAID);
        payout.setReference(reference.trim());
        payout.setPaidAt(clock.instant());
        User artisan = payout.getMaster().getUser();
        if (artisan != null) {
            notificationService.notify(artisan, PAYOUT_PAID, "/artisan/payouts", null,
                    payout.getAmount().toPlainString(), payout.getCurrency());
        }
        return PayoutMapper.toResponse(payout);
    }

    /** Creates the pending payouts of every delivered line without one; returns the new payouts (maybe none). */
    @Transactional
    public List<PayoutResponse> createFromDeliveredLines() {
        Map<String, List<OrderItem>> groups = new LinkedHashMap<>();
        for (OrderItem line : orderItemRepository.findDeliveredWithoutPayout(FulfillmentStatus.DELIVERED)) {
            String key = line.getStore().getMaster().getMasterId() + "|" + line.getCurrency();
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(line);
        }
        List<PayoutResponse> created = new ArrayList<>();
        for (List<OrderItem> lines : groups.values()) {
            created.add(PayoutMapper.toResponse(create(lines.get(0).getStore().getMaster(), lines)));
        }
        return created;
    }

    private Payout create(Master master, List<OrderItem> lines) {
        Payout payout = new Payout();
        payout.setMaster(master);
        payout.setCurrency(lines.get(0).getCurrency());
        payout.setStatus(PayoutStatus.PENDING);
        payout.setAmount(lines.stream().map(OrderItem::getArtisanNetAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        payoutRepository.save(payout);
        for (OrderItem line : lines) {
            PayoutItem item = new PayoutItem();
            item.setPayout(payout);
            item.setOrderItem(line);
            item.setAllocatedNetAmount(line.getArtisanNetAmount());
            item.setCurrency(line.getCurrency());
            payoutItemRepository.save(item);
        }
        return payout;
    }
}
