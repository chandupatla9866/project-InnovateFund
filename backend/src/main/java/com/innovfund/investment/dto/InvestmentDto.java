package com.innovfund.investment.dto;

import com.innovfund.investment.entity.InvestmentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InvestmentDto(
        UUID id,
        UUID startupId,
        String startupName,
        UUID investorId,
        String investorName,
        BigDecimal amount,
        String notes,
        InvestmentStatus status,
        String paymentLinkUrl,
        Instant paidAt,
        Instant createdAt
) {
}
