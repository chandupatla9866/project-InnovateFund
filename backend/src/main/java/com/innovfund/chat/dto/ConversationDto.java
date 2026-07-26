package com.innovfund.chat.dto;

import java.time.Instant;
import java.util.UUID;

public record ConversationDto(
        UUID counterpartId,
        String counterpartName,
        String lastMessage,
        Instant lastMessageAt,
        long unreadCount
) {
}
