package com.innovfund.investor.dto;

import java.time.Instant;
import java.util.UUID;

public record InvestorProfileDto(
        UUID id,
        UUID userId,
        String email,
        String fullName,
        String bio,
        String firmName,
        String investmentInterests,
        boolean verified,
        Instant createdAt
) {
}
