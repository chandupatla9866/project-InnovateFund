package com.innovfund.ai.matching;

import java.util.List;
import java.util.UUID;

public record MatchDto(
        UUID id,
        String name,
        String subtitle,
        double matchPercent,
        List<String> reasons
) {
}
