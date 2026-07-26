package com.innovfund.ai.mentor;

import com.innovfund.common.AccessDeniedCustomException;
import com.innovfund.security.SecurityUser;
import com.innovfund.startup.entity.Startup;
import com.innovfund.startup.service.StartupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/startups/{startupId}/mentor")
@RequiredArgsConstructor
public class MentorController {

    private final MentorService mentorService;
    private final StartupService startupService;

    @PostMapping("/ask")
    @PreAuthorize("hasRole('FOUNDER')")
    public MentorAnswer ask(@AuthenticationPrincipal SecurityUser principal, @PathVariable UUID startupId,
                             @Valid @RequestBody MentorQuestionRequest request) {
        Startup startup = startupService.findOrThrow(startupId);
        if (!startup.getFounder().getId().equals(principal.getUser().getId())) {
            throw new AccessDeniedCustomException("You do not own this startup");
        }
        return mentorService.answer(startup, request.question());
    }
}
