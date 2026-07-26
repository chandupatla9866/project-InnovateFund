package com.innovfund.startup.controller;

import com.innovfund.common.PageResponse;
import com.innovfund.security.SecurityUser;
import com.innovfund.startup.dto.CreateStartupRequest;
import com.innovfund.startup.dto.StartupDto;
import com.innovfund.startup.dto.StartupSummaryDto;
import com.innovfund.startup.dto.UpdateStartupRequest;
import com.innovfund.startup.entity.StartupStage;
import com.innovfund.startup.service.StartupService;
import com.innovfund.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/startups")
@RequiredArgsConstructor
public class StartupController {

    private final StartupService startupService;

    @PostMapping
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<StartupDto> create(@AuthenticationPrincipal SecurityUser principal,
                                              @Valid @RequestBody CreateStartupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(startupService.create(principal.getUser(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FOUNDER')")
    public StartupDto update(@AuthenticationPrincipal SecurityUser principal,
                              @PathVariable UUID id,
                              @Valid @RequestBody UpdateStartupRequest request) {
        return startupService.update(principal.getUser(), id, request);
    }

    @GetMapping("/{id}")
    public StartupDto getById(@AuthenticationPrincipal SecurityUser principal, @PathVariable UUID id) {
        User viewer = principal == null ? null : principal.getUser();
        StartupDto dto = startupService.getById(viewer, id);
        startupService.recordView(viewer, id);
        return dto;
    }

    @GetMapping
    public PageResponse<StartupSummaryDto> listPublished(@RequestParam(required = false) String industry,
                                                           @RequestParam(required = false) StartupStage stage,
                                                           @RequestParam(required = false) String country,
                                                           @RequestParam(required = false) java.math.BigDecimal minFunding,
                                                           @RequestParam(required = false) java.math.BigDecimal maxFunding,
                                                           @RequestParam(required = false) Double minAiScore,
                                                           Pageable pageable) {
        return startupService.listPublished(industry, stage, country, minFunding, maxFunding, minAiScore, pageable);
    }

    @GetMapping("/search")
    public PageResponse<StartupSummaryDto> naturalLanguageSearch(@RequestParam String q, Pageable pageable) {
        return startupService.naturalLanguageSearch(q, pageable);
    }

    @GetMapping("/trending")
    public List<StartupSummaryDto> trending(@RequestParam(defaultValue = "10") int limit) {
        return startupService.trending(limit);
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('FOUNDER')")
    public List<StartupSummaryDto> listMine(@AuthenticationPrincipal SecurityUser principal) {
        return startupService.listMine(principal.getUser());
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasRole('FOUNDER')")
    public StartupDto publish(@AuthenticationPrincipal SecurityUser principal, @PathVariable UUID id) {
        return startupService.publish(principal.getUser(), id);
    }

    @PatchMapping("/{id}/unpublish")
    @PreAuthorize("hasRole('FOUNDER')")
    public StartupDto unpublish(@AuthenticationPrincipal SecurityUser principal, @PathVariable UUID id) {
        return startupService.unpublish(principal.getUser(), id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal SecurityUser principal, @PathVariable UUID id) {
        startupService.delete(principal.getUser(), id);
        return ResponseEntity.noContent().build();
    }
}
