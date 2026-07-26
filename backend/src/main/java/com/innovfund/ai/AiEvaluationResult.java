package com.innovfund.ai;

import java.util.List;
import java.util.Map;

public record AiEvaluationResult(
        Map<RubricCategory, CategoryScore> categoryScores,
        double overallScore,
        String summaryText,
        List<String> strengths,
        List<String> suggestions,
        String investorReadinessStatus,
        String investorReadinessConfidence,
        String modelVersion
) {
}
