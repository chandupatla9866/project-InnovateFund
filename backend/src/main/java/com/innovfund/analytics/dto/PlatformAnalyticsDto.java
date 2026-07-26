package com.innovfund.analytics.dto;

import java.math.BigDecimal;

public record PlatformAnalyticsDto(
        long totalUsers,
        long totalFounders,
        long totalInvestors,
        long totalStartups,
        long totalPublishedStartups,
        long pendingStartupVerifications,
        long pendingFounderVerifications,
        long pendingInvestorVerifications,
        long totalPosts,
        long totalInvestments,
        BigDecimal totalInvestmentVolume,
        long totalMeetings,
        long pendingReports
) {
}
