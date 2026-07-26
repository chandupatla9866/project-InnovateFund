package com.innovfund.analytics.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record StartupAnalyticsDto(
        UUID id,
        String name,
        long viewCount,
        long followerCount,
        long likeCount,
        long interestedInvestorsCount,
        BigDecimal fundingGoal,
        BigDecimal fundingProgress,
        Double latestAiScore
) {
}
