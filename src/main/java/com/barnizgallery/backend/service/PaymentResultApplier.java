package com.barnizgallery.backend.service;

import java.util.EnumSet;
import java.util.Set;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import com.barnizgallery.backend.entity.Payment;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.enums.PaymentStatus;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.exception.DatabaseErrors;
import com.barnizgallery.backend.patterns.adapter.GatewayEvent;
import com.barnizgallery.backend.repository.PaymentRepository;

/**
 * Applies the result of a gateway event to its payment. Only a pending payment changes. The database does the
 * rest in the same transaction: an approved payment marks its obligation {@code pagada}; when every obligation of
 * the order is paid it consumes the reserved stock and moves the order to {@code pagado}. Stock is never changed
 * here. If the database refuses the approval (for example the reservation is gone) the event is answered with 409
 * and nothing is saved, so the gateway retries it.
 */
@Component
public class PaymentResultApplier {

    private static final Set<PaymentStatus> FINAL = EnumSet.of(PaymentStatus.APPROVED, PaymentStatus.REJECTED,
            PaymentStatus.VOIDED);

    private final PaymentRepository paymentRepository;

    public PaymentResultApplier(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /**
     * Returns true when the payment changed. A declined or expired payment lets the database decide whether the
     * reservations are released: it keeps them while their time is not over, so the buyer can try again.
     */
    public boolean apply(Payment payment, GatewayEvent event) {
        if (payment.getStatus() != PaymentStatus.PENDING || !FINAL.contains(event.status())) {
            return false;
        }
        payment.setStatus(event.status());
        payment.setGatewayTransactionId(event.gatewayTransactionId());
        payment.setRawResponse(event.payload());
        try {
            paymentRepository.saveAndFlush(payment);
        } catch (DataAccessException ex) {
            if (DatabaseErrors.isRuleViolation(ex) && event.status() == PaymentStatus.APPROVED) {
                throw new BusinessRuleException(ErrorCode.PAYMENT_APPROVAL_REJECTED, event.reference());
            }
            throw ex;
        }
        return true;
    }
}
