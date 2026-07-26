package com.innovfund.event.dto;

import com.innovfund.event.entity.EventType;

import java.time.Instant;
import java.util.UUID;

public record EventDto(
        UUID id,
        EventType type,
        String title,
        String description,
        Instant eventDate,
        String location,
        String link,
        Instant createdAt
) {
}
