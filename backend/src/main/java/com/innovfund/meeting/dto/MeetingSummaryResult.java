package com.innovfund.meeting.dto;

import java.util.List;

public record MeetingSummaryResult(
        List<String> amountsMentioned,
        List<String> concerns,
        List<String> actionItems,
        String summary,
        String modelVersion
) {
}
