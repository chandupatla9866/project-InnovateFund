package com.innovfund.startup.dto;

import com.innovfund.startup.entity.StartupStage;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record UpdateStartupRequest(
        @NotBlank String name,
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
        String pitchDeckUrl,
        String demoVideoUrl,
        BigDecimal equityOffered,
        String websiteUrl,
        String socialLinks
) {
}
