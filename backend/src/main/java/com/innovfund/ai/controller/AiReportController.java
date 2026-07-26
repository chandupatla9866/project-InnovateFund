package com.innovfund.ai.controller;

import com.innovfund.ai.dto.AiReportDto;
import com.innovfund.ai.dto.AiReportSummaryDto;
import com.innovfund.ai.service.AiReportService;
import com.innovfund.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/startups/{startupId}")
@RequiredArgsConstructor
public class AiReportController {

    private final AiReportService aiReportService;

    @PostMapping("/analyze")
    @PreAuthorize("hasRole('FOUNDER')")
    public AiReportDto analyze(@AuthenticationPrincipal SecurityUser principal, @PathVariable UUID startupId) {
        return aiReportService.analyze(principal.getUser(), startupId);
    }

    @GetMapping("/reports")
    public List<AiReportDto> history(@AuthenticationPrincipal SecurityUser principal, @PathVariable UUID startupId) {
        return aiReportService.history(principal == null ? null : principal.getUser(), startupId);
    }

    @GetMapping("/reports/latest")
    public AiReportDto latest(@AuthenticationPrincipal SecurityUser principal, @PathVariable UUID startupId) {
        return aiReportService.latest(principal == null ? null : principal.getUser(), startupId);
    }

    @GetMapping("/reports/summary")
    public ResponseEntity<AiReportSummaryDto> summary(@PathVariable UUID startupId) {
        AiReportSummaryDto summary = aiReportService.summary(startupId);
        return summary != null ? ResponseEntity.ok(summary) : ResponseEntity.noContent().build();
    }
}
