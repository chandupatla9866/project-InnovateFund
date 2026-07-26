package com.innovfund.diligence.dto;

import com.innovfund.diligence.entity.DueDiligenceStatus;

import java.time.Instant;
import java.util.UUID;

public record DueDiligenceRequestDto(
        UUID id,
        UUID investorId,
        String investorName,
        UUID startupId,
        String startupName,
        DueDiligenceStatus status,
        Instant createdAt,
        Instant respondedAt
) {
}
