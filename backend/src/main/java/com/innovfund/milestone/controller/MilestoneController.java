package com.innovfund.milestone.controller;

import com.innovfund.milestone.dto.CreateMilestoneRequest;
import com.innovfund.milestone.dto.MilestoneDto;
import com.innovfund.milestone.dto.UpdateMilestoneRequest;
import com.innovfund.milestone.service.MilestoneService;
import com.innovfund.security.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/startups/{startupId}/milestones")
@RequiredArgsConstructor
public class MilestoneController {

    private final MilestoneService milestoneService;

    @PostMapping
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<MilestoneDto> create(@AuthenticationPrincipal SecurityUser principal,
                                                @PathVariable UUID startupId,
                                                @Valid @RequestBody CreateMilestoneRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(milestoneService.create(principal.getUser(), startupId, request));
    }

    @GetMapping
    public List<MilestoneDto> list(@PathVariable UUID startupId) {
        return milestoneService.listForStartup(startupId);
    }

    @PutMapping("/{milestoneId}")
    @PreAuthorize("hasRole('FOUNDER')")
    public MilestoneDto update(@AuthenticationPrincipal SecurityUser principal,
                                @PathVariable UUID startupId, @PathVariable UUID milestoneId,
                                @Valid @RequestBody UpdateMilestoneRequest request) {
        return milestoneService.update(principal.getUser(), milestoneId, request);
    }

    @PatchMapping("/{milestoneId}/complete")
    @PreAuthorize("hasRole('FOUNDER')")
    public MilestoneDto toggleComplete(@AuthenticationPrincipal SecurityUser principal,
                                        @PathVariable UUID startupId, @PathVariable UUID milestoneId) {
        return milestoneService.toggleComplete(principal.getUser(), milestoneId);
    }

    @DeleteMapping("/{milestoneId}")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal SecurityUser principal,
                                        @PathVariable UUID startupId, @PathVariable UUID milestoneId) {
        milestoneService.delete(principal.getUser(), milestoneId);
        return ResponseEntity.noContent().build();
    }
}
