package com.innovfund.founder.dto;

import java.time.Instant;
import java.util.UUID;

public record FounderProfileDto(
        UUID id,
        UUID userId,
        String email,
        String fullName,
        String bio,
        String phone,
        String linkedinUrl,
        boolean verified,
        Instant createdAt
) {
}
