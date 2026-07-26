package com.innovfund.ai.research;

import jakarta.validation.constraints.NotBlank;

public record MarketResearchRequest(@NotBlank String query) {
}
