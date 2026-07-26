package com.innovfund.ai.research;

import java.util.List;

public record MarketResearchResult(
        String query,
        List<String> topCompetitors,
        String estimatedGrowth,
        List<String> majorChallenges,
        String note,
        String modelVersion
) {
}
