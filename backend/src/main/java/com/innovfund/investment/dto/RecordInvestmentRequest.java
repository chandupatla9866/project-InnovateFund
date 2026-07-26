package com.innovfund.investment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record RecordInvestmentRequest(
        @NotNull UUID investorId,
        // Razorpay payment links require a minimum of ₹1 — matching that here so validation fails
        // with a clear message instead of surfacing as a confusing Razorpay API error later.
        @NotNull @DecimalMin(value = "1") BigDecimal amount,
        String notes
) {
}
