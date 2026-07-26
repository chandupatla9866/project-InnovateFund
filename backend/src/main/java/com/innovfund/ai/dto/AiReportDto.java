package com.innovfund.ai.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AiReportDto(
        UUID id,
        UUID startupId,
        double overallScore,
        List<CategoryScoreDto> categoryScores,
        List<String> strengths,
        List<String> suggestions,
        String summaryText,
        String investorReadinessStatus,
        String investorReadinessConfidence,
        String modelVersion,
        Instant createdAt
) {
}
