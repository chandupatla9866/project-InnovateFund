package com.innovfund.report.dto;

import com.innovfund.report.entity.ReportStatus;
import com.innovfund.report.entity.ReportTargetType;

import java.time.Instant;
import java.util.UUID;

public record ReportDto(
        UUID id,
        UUID reporterId,
        String reporterName,
        ReportTargetType targetType,
        UUID targetId,
        String reason,
        ReportStatus status,
        Instant createdAt
) {
}
