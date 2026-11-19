package com.barnizgallery.backend.entity;

import java.math.BigDecimal;
import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.barnizgallery.backend.converter.PaymentObligationStageConverter;
import com.barnizgallery.backend.converter.PaymentObligationStatusConverter;
import com.barnizgallery.backend.enums.PaymentObligationStage;
import com.barnizgallery.backend.enums.PaymentObligationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * An amount the buyer must pay for an order: a single payment, or a deposit and a balance for
 * custom orders. Payments are made per obligation. Maps the table {@code museo.payment_obligations}.
 */
@Entity
@Table(name = "payment_obligations")
public class PaymentObligation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "obligation_id")
    private Long obligationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Convert(converter = PaymentObligationStageConverter.class)
    @Column(name = "stage", nullable = false, length = 20)
    private PaymentObligationStage stage;

    @Column(name = "basis_amount", precision = 14, scale = 2)
    private BigDecimal basisAmount;

    @Column(name = "basis_currency", length = 3)
    private String basisCurrency;

    @Column(name = "amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Convert(converter = PaymentObligationStatusConverter.class)
    @Column(name = "status", nullable = false, length = 30)
    private PaymentObligationStatus status;

    @Column(name = "due_at")
    private Instant dueAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Long getObligationId() {
        return obligationId;
    }

    public void setObligationId(Long obligationId) {
        this.obligationId = obligationId;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public PaymentObligationStage getStage() {
        return stage;
    }

    public void setStage(PaymentObligationStage stage) {
        this.stage = stage;
    }

    public BigDecimal getBasisAmount() {
        return basisAmount;
    }

    public void setBasisAmount(BigDecimal basisAmount) {
        this.basisAmount = basisAmount;
    }

    public String getBasisCurrency() {
        return basisCurrency;
    }

    public void setBasisCurrency(String basisCurrency) {
        this.basisCurrency = basisCurrency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PaymentObligationStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentObligationStatus status) {
        this.status = status;
    }

    public Instant getDueAt() {
        return dueAt;
    }

    public void setDueAt(Instant dueAt) {
        this.dueAt = dueAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
