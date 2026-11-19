package com.barnizgallery.backend.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Catalog budget range with its own currency. Maps the table {@code museo.budget_ranges}.
 */
@Entity
@Table(name = "budget_ranges")
public class BudgetRange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "budget_range_id")
    private Long budgetRangeId;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "min_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal minAmount;

    @Column(name = "max_amount", precision = 14, scale = 2)
    private BigDecimal maxAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    public Long getBudgetRangeId() {
        return budgetRangeId;
    }

    public void setBudgetRangeId(Long budgetRangeId) {
        this.budgetRangeId = budgetRangeId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
