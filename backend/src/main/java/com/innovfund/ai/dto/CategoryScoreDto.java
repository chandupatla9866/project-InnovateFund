package com.innovfund.ai.dto;

public record CategoryScoreDto(String category, String displayName, double weight, double rawScore, double weightedScore, String reasoning) {
}
