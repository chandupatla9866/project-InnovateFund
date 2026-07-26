package com.innovfund.startup.entity;

import com.innovfund.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "startups", indexes = {
        @Index(name = "idx_startups_founder_id", columnList = "founder_id"),
        @Index(name = "idx_startups_published", columnList = "published")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Startup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "founder_id", nullable = false)
    private User founder;

    @Column(nullable = false)
    private String name;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    private String industry;

    private String country;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50)")
    private StartupStage stage;

    @Column(columnDefinition = "TEXT")
    private String problem;

    @Column(columnDefinition = "TEXT")
    private String solution;

    @Column(name = "business_model", columnDefinition = "TEXT")
    private String businessModel;

    @Column(name = "revenue_model", columnDefinition = "TEXT")
    private String revenueModel;

    @Column(name = "target_audience", columnDefinition = "TEXT")
    private String targetAudience;

    @Column(columnDefinition = "TEXT")
    private String market;

    @Column(columnDefinition = "TEXT")
    private String competitors;

    @Column(name = "funding_goal", precision = 15, scale = 2)
    private BigDecimal fundingGoal;

    @Builder.Default
    @Column(name = "funding_progress", precision = 15, scale = 2)
    private BigDecimal fundingProgress = BigDecimal.ZERO;

    @Column(name = "pitch_deck_url")
    private String pitchDeckUrl;

    @Column(name = "demo_video_url")
    private String demoVideoUrl;

    @Column(name = "equity_offered", precision = 5, scale = 2)
    private BigDecimal equityOffered;

    @Column(name = "website_url")
    private String websiteUrl;

    @Column(name = "social_links", columnDefinition = "TEXT")
    private String socialLinks;

    @Builder.Default
    @Column(nullable = false)
    private boolean published = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean verified = false;

    @Builder.Default
    @Column(name = "view_count", nullable = false)
    private long viewCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
