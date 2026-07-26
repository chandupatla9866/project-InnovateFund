package com.innovfund.meeting.dto;

import jakarta.validation.constraints.NotBlank;

public record SummarizeMeetingRequest(@NotBlank String transcript) {
}
