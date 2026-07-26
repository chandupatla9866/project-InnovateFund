package com.innovfund.team.dto;

import jakarta.validation.constraints.NotBlank;

public record TeamMemberRequest(
        @NotBlank String name,
        String role,
        String bio,
        String photoUrl,
        int displayOrder
) {
}
