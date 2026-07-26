package com.innovfund.meeting.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record CreateMeetingRequest(
        @NotNull UUID recipientId,
        UUID startupId,
        @NotNull @Future Instant scheduledAt,
        Integer durationMinutes,
        String notes
) {
}
