package com.innovfund.user.dto;

import com.innovfund.user.entity.Role;

import java.util.UUID;

public record UserSummaryDto(UUID id, String email, Role role, String fullName) {
}
