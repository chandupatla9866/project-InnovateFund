package com.innovfund.feed.controller;

import com.innovfund.feed.service.FollowService;
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
public class FollowController {

    private final FollowService followService;

    @PostMapping("/api/startups/{id}/follow")
    @PreAuthorize("hasRole('INVESTOR')")
    public ResponseEntity<Void> follow(@AuthenticationPrincipal SecurityUser principal, @PathVariable UUID id) {
        followService.follow(principal.getUser(), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/startups/{id}/follow")
    @PreAuthorize("hasRole('INVESTOR')")
    public ResponseEntity<Void> unfollow(@AuthenticationPrincipal SecurityUser principal, @PathVariable UUID id) {
        followService.unfollow(principal.getUser(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/startups/{id}/followers/count")
    public long followerCount(@PathVariable UUID id) {
        return followService.followerCount(id);
    }

    @GetMapping("/api/investors/me/following")
    @PreAuthorize("hasRole('INVESTOR')")
    public List<StartupSummaryDto> following(@AuthenticationPrincipal SecurityUser principal) {
        return followService.following(principal.getUser());
    }
}
