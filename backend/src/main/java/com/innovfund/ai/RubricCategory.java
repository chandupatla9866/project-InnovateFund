package com.innovfund.ai;

public enum RubricCategory {
    PROBLEM_CLARITY(0.15, "Problem Clarity"),
    SOLUTION_QUALITY(0.15, "Solution Quality"),
    MARKET_SIZE(0.15, "Market Size"),
    BUSINESS_MODEL(0.15, "Business Model"),
    COMPETITOR_ANALYSIS(0.10, "Competitor Analysis"),
    TEAM_STRENGTH(0.10, "Team Strength"),
    FINANCIAL_PLANNING(0.10, "Financial Planning"),
    SCALABILITY(0.10, "Scalability"),
    FUNDING_JUSTIFICATION(0.10, "Funding Justification");

    private final double weight;
    private final String displayName;

    RubricCategory(double weight, String displayName) {
        this.weight = weight;
        this.displayName = displayName;
    }

    public double getWeight() {
        return weight;
    }

    public String getDisplayName() {
        return displayName;
    }
}
