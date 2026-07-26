package com.innovfund.startup.dto;

import com.innovfund.startup.entity.StartupStage;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record StartupDto(
        UUID id,
        UUID founderId,
        String founderName,
        String name,
        String logoUrl,
        String coverImageUrl,
        String industry,
        String country,
        StartupStage stage,
        String problem,
        String solution,
        String businessModel,
        String revenueModel,
        String targetAudience,
        String market,
        String competitors,
        BigDecimal fundingGoal,
        BigDecimal fundingProgress,
        String pitchDeckUrl,
        String demoVideoUrl,
        BigDecimal equityOffered,
        String websiteUrl,
        String socialLinks,
        boolean published,
        boolean verified,
        long interestedInvestorsCount,
        long viewCount,
        Instant createdAt,
        Instant updatedAt
) {
}
