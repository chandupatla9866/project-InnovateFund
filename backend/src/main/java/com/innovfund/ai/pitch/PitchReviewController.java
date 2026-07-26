package com.innovfund.ai.pitch;

import com.innovfund.common.AccessDeniedCustomException;
import com.innovfund.security.SecurityUser;
import com.innovfund.startup.entity.Startup;
import com.innovfund.startup.service.StartupService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/startups/{startupId}/pitch-review")
@RequiredArgsConstructor
public class PitchReviewController {

    private final PitchReviewService pitchReviewService;
    private final PitchImprovementService pitchImprovementService;
    private final StartupService startupService;

    @PostMapping
    @PreAuthorize("hasRole('FOUNDER')")
    public PitchReviewResult review(@AuthenticationPrincipal SecurityUser principal, @PathVariable UUID startupId) {
        return pitchReviewService.review(getOwnedStartup(principal, startupId));
    }

    @PostMapping("/improve")
    @PreAuthorize("hasRole('FOUNDER')")
    public PitchImprovementResult improve(@AuthenticationPrincipal SecurityUser principal, @PathVariable UUID startupId) {
        return pitchImprovementService.improve(getOwnedStartup(principal, startupId));
    }

    private Startup getOwnedStartup(SecurityUser principal, UUID startupId) {
        Startup startup = startupService.findOrThrow(startupId);
        if (!startup.getFounder().getId().equals(principal.getUser().getId())) {
            throw new AccessDeniedCustomException("You do not own this startup");
        }
        return startup;
    }
}
