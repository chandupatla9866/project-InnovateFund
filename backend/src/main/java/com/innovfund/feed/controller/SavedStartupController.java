package com.innovfund.feed.controller;

import com.innovfund.feed.service.SavedStartupService;
import com.innovfund.security.SecurityUser;
import com.innovfund.startup.dto.StartupSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class SavedStartupController {

    private final SavedStartupService savedStartupService;

    @PostMapping("/api/startups/{id}/save")
    @PreAuthorize("hasRole('INVESTOR')")
    public ResponseEntity<Void> save(@AuthenticationPrincipal SecurityUser principal, @PathVariable UUID id) {
        savedStartupService.save(principal.getUser(), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/startups/{id}/save")
    @PreAuthorize("hasRole('INVESTOR')")
    public ResponseEntity<Void> unsave(@AuthenticationPrincipal SecurityUser principal, @PathVariable UUID id) {
        savedStartupService.unsave(principal.getUser(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/investors/me/saved-startups")
    @PreAuthorize("hasRole('INVESTOR')")
    public List<StartupSummaryDto> mySaved(@AuthenticationPrincipal SecurityUser principal) {
        return savedStartupService.mySaved(principal.getUser());
    }
}
