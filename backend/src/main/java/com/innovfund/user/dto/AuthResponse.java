package com.innovfund.user.dto;

public record AuthResponse(String token, UserSummaryDto user) {
}
