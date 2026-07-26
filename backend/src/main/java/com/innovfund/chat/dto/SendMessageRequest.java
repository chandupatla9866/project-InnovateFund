package com.innovfund.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SendMessageRequest(@NotNull UUID recipientId, @NotBlank String text) {
}
