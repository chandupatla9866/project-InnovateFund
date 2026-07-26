package com.innovfund.startup.dto;

import com.innovfund.startup.entity.StartupStage;

import java.math.BigDecimal;
import java.util.UUID;

public record StartupSummaryDto(
        UUID id,
        String name,
        String logoUrl,
        String industry,
        StartupStage stage,
        BigDecimal fundingGoal,
        BigDecimal fundingProgress,
        boolean published,
        boolean verified,
        long interestedInvestorsCount
) {
}
