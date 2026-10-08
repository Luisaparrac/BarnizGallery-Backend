package com.barnizgallery.backend.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * A room recommended to a visitor with a score from 0 to 100. Maps the existing table {@code recommendations}.
 */
@Entity
@Table(name = "recommendations")
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendation_id")
    private Integer recommendationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "visitor_id", nullable = false)
    private Visitor visitor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "reason", columnDefinition = "text")
    private String reason;

    @Column(name = "recommendation_date")
    private LocalDateTime recommendationDate;

    /** The column has a DB default, but the value is set from Java so we do not depend on it. */
    @PrePersist
    void onCreate() {
        if (recommendationDate == null) {
            recommendationDate = LocalDateTime.now();
        }
    }

    public Integer getRecommendationId() {
        return recommendationId;
    }

    public void setRecommendationId(Integer recommendationId) {
        this.recommendationId = recommendationId;
    }

    public Visitor getVisitor() {
        return visitor;
    }

    public void setVisitor(Visitor visitor) {
        this.visitor = visitor;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getRecommendationDate() {
        return recommendationDate;
    }

    public void setRecommendationDate(LocalDateTime recommendationDate) {
        this.recommendationDate = recommendationDate;
    }
}
