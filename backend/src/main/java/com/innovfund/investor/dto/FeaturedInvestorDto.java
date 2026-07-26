package com.innovfund.investor.dto;

import java.util.UUID;

public record FeaturedInvestorDto(
        UUID id,
        String fullName,
        String firmName,
        String investmentInterests
) {
}
