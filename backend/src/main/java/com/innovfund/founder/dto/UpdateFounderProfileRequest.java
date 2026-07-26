package com.innovfund.founder.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateFounderProfileRequest(
        @NotBlank String fullName,
        String bio,
        String phone,
        String linkedinUrl
) {
}
