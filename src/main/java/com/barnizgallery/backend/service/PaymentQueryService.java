package com.barnizgallery.backend.service;

import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.PaymentDetailResponse;
import com.barnizgallery.backend.entity.Payment;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.enums.PaymentStatus;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.repository.PaymentRepository;
import com.barnizgallery.backend.security.CurrentUser;

/**
 * A payment of an order of the logged-in buyer; a payment of someone else is answered as not found.
 */
@Service
public class PaymentQueryService {

    private final PaymentRepository paymentRepository;
    private final CurrentUser currentUser;

    public PaymentQueryService(PaymentRepository paymentRepository, CurrentUser currentUser) {
        this.paymentRepository = paymentRepository;
        this.currentUser = currentUser;
    }

    @Transactional(readOnly = true)
    public PaymentDetailResponse mine(Long paymentId) {
        Long buyerId = currentUser.requireId();
        Payment payment = paymentRepository.findById(paymentId)
                .filter(p -> p.getObligation().getOrder().getBuyer().getUserId().equals(buyerId))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PAYMENT_NOT_FOUND, paymentId));
        return new PaymentDetailResponse(payment.getPaymentId(), payment.getObligation().getOrder().getOrderId(),
                payment.getObligation().getObligationId(), payment.getGateway().toUpperCase(Locale.ROOT),
                PaymentReferences.of(payment.getPaymentId()), payment.getPaymentMethod(), payment.getStatus(),
                payment.getAmount(), payment.getCurrency(), null, payment.getCreatedAt(),
                payment.getStatus() == PaymentStatus.APPROVED ? payment.getUpdatedAt() : null);
    }
}
