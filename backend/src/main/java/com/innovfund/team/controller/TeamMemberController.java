package com.innovfund.team.controller;

import com.innovfund.security.SecurityUser;
import com.innovfund.team.dto.TeamMemberDto;
import com.innovfund.team.dto.TeamMemberRequest;
import com.innovfund.team.service.TeamMemberService;
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
@RequestMapping("/api/startups/{startupId}/team")
@RequiredArgsConstructor
public class TeamMemberController {

    private final TeamMemberService teamMemberService;

    @PostMapping
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<TeamMemberDto> create(@AuthenticationPrincipal SecurityUser principal,
                                                 @PathVariable UUID startupId,
                                                 @Valid @RequestBody TeamMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teamMemberService.create(principal.getUser(), startupId, request));
    }

    @GetMapping
    public List<TeamMemberDto> list(@PathVariable UUID startupId) {
        return teamMemberService.listForStartup(startupId);
    }

    @PutMapping("/{memberId}")
    @PreAuthorize("hasRole('FOUNDER')")
    public TeamMemberDto update(@AuthenticationPrincipal SecurityUser principal,
                                 @PathVariable UUID startupId, @PathVariable UUID memberId,
                                 @Valid @RequestBody TeamMemberRequest request) {
        return teamMemberService.update(principal.getUser(), memberId, request);
    }

    @DeleteMapping("/{memberId}")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal SecurityUser principal,
                                        @PathVariable UUID startupId, @PathVariable UUID memberId) {
        teamMemberService.delete(principal.getUser(), memberId);
        return ResponseEntity.noContent().build();
    }
}
