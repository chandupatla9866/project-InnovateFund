package com.innovfund.interest.dto;

import java.time.Instant;
import java.util.UUID;

public record InterestedInvestorDto(
        UUID investorId,
        String investorName,
        String firmName,
        String investmentInterests,
        boolean verified,
        long pastInvestmentsCount,
        Double aiMatchPercent,
        String status,
        Instant expressedAt
) {
}
