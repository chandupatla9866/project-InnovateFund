package com.innovfund.ai.matching;

import com.innovfund.common.AccessDeniedCustomException;
import com.innovfund.common.ResourceNotFoundException;
import com.innovfund.investor.entity.InvestorProfile;
import com.innovfund.investor.repository.InvestorProfileRepository;
import com.innovfund.security.SecurityUser;
import com.innovfund.startup.entity.Startup;
import com.innovfund.startup.service.StartupService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;
    private final StartupService startupService;
    private final InvestorProfileRepository investorProfileRepository;

    @GetMapping("/api/startups/{startupId}/matches")
    @PreAuthorize("hasRole('FOUNDER')")
    public List<MatchDto> matchesForStartup(@AuthenticationPrincipal SecurityUser principal, @PathVariable UUID startupId) {
        Startup startup = startupService.findOrThrow(startupId);
        if (!startup.getFounder().getId().equals(principal.getUser().getId())) {
            throw new AccessDeniedCustomException("You do not own this startup");
        }
        return matchingService.matchInvestorsForStartup(startup);
    }

    @GetMapping("/api/investors/me/matches")
    @PreAuthorize("hasRole('INVESTOR')")
    public List<MatchDto> matchesForInvestor(@AuthenticationPrincipal SecurityUser principal) {
        InvestorProfile profile = investorProfileRepository.findByUserId(principal.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Investor profile not found"));
        return matchingService.matchStartupsForInvestor(principal.getUser(), profile);
    }
}
