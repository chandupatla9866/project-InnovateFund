package com.innovfund.startup.search;

import com.innovfund.startup.entity.StartupStage;

import java.math.BigDecimal;

public record StartupSearchFilters(String keyword, StartupStage stage, BigDecimal minFunding, BigDecimal maxFunding) {
}
