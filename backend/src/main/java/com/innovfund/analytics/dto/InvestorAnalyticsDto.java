package com.innovfund.analytics.dto;

import java.math.BigDecimal;

public record InvestorAnalyticsDto(
        long followingCount,
        long interestsCount,
        long totalMeetings,
        long upcomingMeetings,
        long unreadMessages,
        long portfolioStartupsCount,
        BigDecimal portfolioInvestedTotal
) {
}
