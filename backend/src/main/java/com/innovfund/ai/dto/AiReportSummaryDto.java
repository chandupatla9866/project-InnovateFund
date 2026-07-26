package com.innovfund.ai.dto;

public record AiReportSummaryDto(
        double overallScore,
        String investorReadinessStatus,
        String industry,
        String stage
) {
}
