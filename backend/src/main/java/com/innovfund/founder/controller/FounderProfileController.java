package com.innovfund.founder.controller;

import com.innovfund.founder.dto.FounderProfileDto;
import com.innovfund.founder.dto.UpdateFounderProfileRequest;
import com.innovfund.founder.service.FounderProfileService;
import com.innovfund.security.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/founders")
@RequiredArgsConstructor
public class FounderProfileController {

    private final FounderProfileService founderProfileService;

    @GetMapping("/me")
    public FounderProfileDto me(@AuthenticationPrincipal SecurityUser principal) {
        return founderProfileService.getByUser(principal.getUser());
    }

    @PutMapping("/me")
    public FounderProfileDto updateMe(@AuthenticationPrincipal SecurityUser principal,
                                       @Valid @RequestBody UpdateFounderProfileRequest request) {
        return founderProfileService.update(principal.getUser(), request);
    }

    @GetMapping("/{id}")
    public FounderProfileDto getById(@PathVariable UUID id) {
        return founderProfileService.getById(id);
    }
}
