package com.innovfund.investor.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateInvestorProfileRequest(
        @NotBlank String fullName,
        String bio,
        String firmName,
        String investmentInterests
) {
}
