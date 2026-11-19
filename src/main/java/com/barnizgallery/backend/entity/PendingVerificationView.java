package com.barnizgallery.backend.entity;

import java.time.Instant;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;
import org.hibernate.annotations.Synchronize;

import com.barnizgallery.backend.converter.MasterVerificationStatusConverter;
import com.barnizgallery.backend.converter.VerificationStepConverter;
import com.barnizgallery.backend.converter.VerificationStepStatusConverter;
import com.barnizgallery.backend.enums.MasterVerificationStatus;
import com.barnizgallery.backend.enums.VerificationStep;
import com.barnizgallery.backend.enums.VerificationStepStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * A master or verification step waiting for review (read-only view {@code museo.v_pending_verifications}).
 * <p>
 * The view has no unique non-null column (a master without steps has no verification id), so it is read
 * with {@code @Subselect} and a synthetic key {@code masterId-verificationId} instead of {@code @Table}.
 * Because of that, the query names the {@code museo} schema explicitly.
 */
@Entity
@Immutable
@Subselect("select concat(p.master_id, '-', coalesce(p.verification_id, 0)) as row_key, p.* "
        + "from museo.v_pending_verifications p")
@Synchronize({ "masters", "master_verifications" })
public class PendingVerificationView {

    @Id
    @Column(name = "row_key")
    private String rowKey;

    @Column(name = "master_id")
    private Long masterId;

    @Column(name = "name", length = 100)
    private String name;

    @Convert(converter = MasterVerificationStatusConverter.class)
    @Column(name = "verification_status", length = 30)
    private MasterVerificationStatus verificationStatus;

    @Column(name = "verification_id")
    private Long verificationId;

    @Convert(converter = VerificationStepConverter.class)
    @Column(name = "step", length = 30)
    private VerificationStep step;

    @Convert(converter = VerificationStepStatusConverter.class)
    @Column(name = "step_status", length = 30)
    private VerificationStepStatus stepStatus;

    @Column(name = "evidence_url")
    private String evidenceUrl;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at")
    private Instant createdAt;

    public String getRowKey() {
        return rowKey;
    }

    public Long getMasterId() {
        return masterId;
    }

    public String getName() {
        return name;
    }

    public MasterVerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public Long getVerificationId() {
        return verificationId;
    }

    public VerificationStep getStep() {
        return step;
    }

    public VerificationStepStatus getStepStatus() {
        return stepStatus;
    }

    public String getEvidenceUrl() {
        return evidenceUrl;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
