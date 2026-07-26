package com.innovfund.event.dto;

import com.innovfund.event.entity.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CreateEventRequest(
        @NotNull EventType type,
        @NotBlank String title,
        String description,
        @NotNull Instant eventDate,
        String location,
        String link
) {
}
