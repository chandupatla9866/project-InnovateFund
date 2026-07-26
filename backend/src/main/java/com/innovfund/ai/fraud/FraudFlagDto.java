package com.innovfund.ai.fraud;

import java.util.List;
import java.util.UUID;

public record FraudFlagDto(
        UUID startupId,
        String startupName,
        String founderName,
        String severity,
        List<String> reasons
) {
}
