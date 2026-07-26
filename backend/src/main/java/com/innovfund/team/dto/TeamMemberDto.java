package com.innovfund.team.dto;

import java.util.UUID;

public record TeamMemberDto(
        UUID id,
        UUID startupId,
        String name,
        String role,
        String bio,
        String photoUrl,
        int displayOrder
) {
}
