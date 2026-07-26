package com.innovfund.ai;

public record CategoryScore(RubricCategory category, double rawScore, double weightedScore, String reasoning) {
}
