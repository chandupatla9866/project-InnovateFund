package com.innovfund.analytics.dto;

import java.math.BigDecimal;
import java.util.List;

public record FounderAnalyticsDto(
        long totalViews,
        long totalFollowers,
        long totalLikes,
        long totalInterestedInvestors,
        BigDecimal totalFundingGoal,
        BigDecimal totalFundingProgress,
        long totalMeetings,
        long upcomingMeetings,
        long unreadMessages,
        List<StartupAnalyticsDto> startups
) {
}
