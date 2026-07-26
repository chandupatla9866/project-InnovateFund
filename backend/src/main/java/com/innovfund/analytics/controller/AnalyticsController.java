package com.innovfund.analytics.controller;

import com.innovfund.analytics.dto.FounderAnalyticsDto;
import com.innovfund.analytics.dto.InvestorAnalyticsDto;
import com.innovfund.analytics.dto.PlatformAnalyticsDto;
import com.innovfund.analytics.service.AnalyticsService;
import com.innovfund.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/api/founders/me/analytics")
    @PreAuthorize("hasRole('FOUNDER')")
    public FounderAnalyticsDto founderAnalytics(@AuthenticationPrincipal SecurityUser principal) {
        return analyticsService.founderAnalytics(principal.getUser());
    }

    @GetMapping("/api/investors/me/analytics")
    @PreAuthorize("hasRole('INVESTOR')")
    public InvestorAnalyticsDto investorAnalytics(@AuthenticationPrincipal SecurityUser principal) {
        return analyticsService.investorAnalytics(principal.getUser());
    }

    @GetMapping("/api/admin/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public PlatformAnalyticsDto platformAnalytics() {
        return analyticsService.platformAnalytics();
    }
}
