package com.innovfund.milestone.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record CreateMilestoneRequest(
        @NotBlank String title,
        String description,
        LocalDate targetDate
) {
}
