package com.innovfund.ai.entity;

import com.innovfund.startup.entity.Startup;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ai_reports", indexes = @Index(name = "idx_ai_reports_startup_id", columnList = "startup_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "startup_id", nullable = false)
    private Startup startup;

    @Column(name = "overall_score", nullable = false)
    private double overallScore;

    @Column(name = "category_scores_json", columnDefinition = "TEXT", nullable = false)
    private String categoryScoresJson;

    @Column(name = "suggestions_json", columnDefinition = "TEXT", nullable = false)
    private String suggestionsJson;

    @Column(name = "summary_text", columnDefinition = "TEXT")
    private String summaryText;

    @Column(name = "strengths_json", columnDefinition = "TEXT")
    private String strengthsJson;

    @Column(name = "investor_readiness_status", columnDefinition = "varchar(100)")
    private String investorReadinessStatus;

    @Column(name = "investor_readiness_confidence", columnDefinition = "varchar(20)")
    private String investorReadinessConfidence;

    @Column(name = "model_version", nullable = false)
    private String modelVersion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
