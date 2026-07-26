package com.innovfund.interest.controller;

import com.innovfund.interest.dto.FounderInterestedInvestorDto;
import com.innovfund.interest.dto.InterestedInvestorDto;
import com.innovfund.interest.service.StartupInterestService;
import com.innovfund.security.SecurityUser;
import com.innovfund.startup.dto.StartupSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class StartupInterestController {

    private final StartupInterestService interestService;

    @PostMapping("/api/startups/{id}/interest")
    @PreAuthorize("hasRole('INVESTOR')")
    public ResponseEntity<Void> express(@AuthenticationPrincipal SecurityUser principal, @PathVariable UUID id) {
        interestService.expressInterest(principal.getUser(), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/startups/{id}/interest")
    @PreAuthorize("hasRole('INVESTOR')")
    public ResponseEntity<Void> withdraw(@AuthenticationPrincipal SecurityUser principal, @PathVariable UUID id) {
        interestService.withdrawInterest(principal.getUser(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/startups/{id}/interest-count")
    public Map<String, Long> count(@PathVariable UUID id) {
        return Map.of("count", interestService.countForStartup(id));
    }

    @GetMapping("/api/startups/{id}/interested-investors")
    @PreAuthorize("hasRole('FOUNDER')")
    public List<InterestedInvestorDto> interestedInvestors(@AuthenticationPrincipal SecurityUser principal, @PathVariable UUID id) {
        return interestService.listForStartup(principal.getUser(), id);
    }

    @PatchMapping("/api/startups/{id}/interested-investors/{investorId}/accept")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<Void> acceptInterest(@AuthenticationPrincipal SecurityUser principal,
                                                @PathVariable UUID id, @PathVariable UUID investorId) {
        interestService.accept(principal.getUser(), id, investorId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/startups/{id}/interested-investors/{investorId}/reject")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<Void> rejectInterest(@AuthenticationPrincipal SecurityUser principal,
                                                @PathVariable UUID id, @PathVariable UUID investorId) {
        interestService.reject(principal.getUser(), id, investorId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/investors/me/interests")
    @PreAuthorize("hasRole('INVESTOR')")
    public List<StartupSummaryDto> myInterests(@AuthenticationPrincipal SecurityUser principal) {
        return interestService.myInterests(principal.getUser());
    }

    @GetMapping("/api/founders/me/interested-investors")
    @PreAuthorize("hasRole('FOUNDER')")
    public List<FounderInterestedInvestorDto> interestedInvestorsForFounder(@AuthenticationPrincipal SecurityUser principal) {
        return interestService.listForFounder(principal.getUser());
    }
}
