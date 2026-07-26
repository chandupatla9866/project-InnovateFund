package com.innovfund.meeting.dto;

import com.innovfund.meeting.entity.MeetingStatus;

import java.time.Instant;
import java.util.UUID;

public record MeetingDto(
        UUID id,
        UUID requesterId,
        String requesterName,
        UUID recipientId,
        String recipientName,
        UUID startupId,
        String startupName,
        Instant scheduledAt,
        int durationMinutes,
        MeetingStatus status,
        String meetingLink,
        String notes,
        Instant createdAt
) {
}
