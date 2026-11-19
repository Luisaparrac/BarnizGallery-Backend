package com.barnizgallery.backend.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * One call to an AI provider: function, provider, model, result, tokens and latency.
 * Maps the table {@code museo.ai_usage_log}.
 */
@Entity
@Table(name = "ai_usage_log")
public class AiUsageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usage_id")
    private Long usageId;

    @Column(name = "function_name", nullable = false)
    private String functionName;

    @Column(name = "provider", nullable = false)
    private String provider;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "success", nullable = false)
    private boolean success;

    @Column(name = "tokens")
    private Integer tokens;

    @Column(name = "latency_ms")
    private Integer latencyMs;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Long getUsageId() {
        return usageId;
    }

    public void setUsageId(Long usageId) {
        this.usageId = usageId;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Integer getTokens() {
        return tokens;
    }

    public void setTokens(Integer tokens) {
        this.tokens = tokens;
    }

    public Integer getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(Integer latencyMs) {
        this.latencyMs = latencyMs;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
