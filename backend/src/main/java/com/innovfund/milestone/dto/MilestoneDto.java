package com.innovfund.milestone.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record MilestoneDto(
        UUID id,
        UUID startupId,
        String title,
        String description,
        LocalDate targetDate,
        boolean completed,
        Instant completedAt,
        Instant createdAt
) {
}
