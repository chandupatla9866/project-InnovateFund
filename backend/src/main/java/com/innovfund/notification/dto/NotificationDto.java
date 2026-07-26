package com.innovfund.notification.dto;

import com.innovfund.notification.entity.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationDto(
        UUID id,
        NotificationType type,
        String message,
        String link,
        boolean read,
        Instant createdAt
) {
}
