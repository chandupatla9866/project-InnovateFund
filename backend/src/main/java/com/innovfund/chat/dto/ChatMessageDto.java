package com.innovfund.chat.dto;

import java.time.Instant;
import java.util.UUID;

public record ChatMessageDto(
        UUID id,
        UUID senderId,
        UUID recipientId,
        String text,
        boolean read,
        Instant createdAt
) {
}
