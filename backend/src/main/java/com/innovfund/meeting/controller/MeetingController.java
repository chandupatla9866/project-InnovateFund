package com.innovfund.meeting.controller;

import com.innovfund.meeting.dto.CreateMeetingRequest;
import com.innovfund.meeting.dto.MeetingDto;
import com.innovfund.meeting.dto.MeetingSummaryResult;
import com.innovfund.meeting.dto.SummarizeMeetingRequest;
import com.innovfund.meeting.service.MeetingService;
import com.innovfund.security.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    @PostMapping
    public ResponseEntity<MeetingDto> request(@AuthenticationPrincipal SecurityUser principal,
                                               @Valid @RequestBody CreateMeetingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(meetingService.request(principal.getUser(), request));
    }

    @GetMapping("/mine")
    public List<MeetingDto> mine(@AuthenticationPrincipal SecurityUser principal) {
        return meetingService.listMine(principal.getUser());
    }

    @PatchMapping("/{id}/accept")
    public MeetingDto accept(@AuthenticationPrincipal SecurityUser principal, @PathVariable UUID id) {
        return meetingService.accept(principal.getUser(), id);
    }

    @PatchMapping("/{id}/reject")
    public MeetingDto reject(@AuthenticationPrincipal SecurityUser principal, @PathVariable UUID id) {
        return meetingService.reject(principal.getUser(), id);
    }

    @PatchMapping("/{id}/cancel")
    public MeetingDto cancel(@AuthenticationPrincipal SecurityUser principal, @PathVariable UUID id) {
        return meetingService.cancel(principal.getUser(), id);
    }

    @PostMapping("/{id}/summarize")
    public MeetingSummaryResult summarize(@AuthenticationPrincipal SecurityUser principal, @PathVariable UUID id,
                                           @Valid @RequestBody SummarizeMeetingRequest request) {
        return meetingService.summarize(principal.getUser(), id, request.transcript());
    }

    @GetMapping("/{id}/summary")
    public MeetingSummaryResult summary(@AuthenticationPrincipal SecurityUser principal, @PathVariable UUID id) {
        return meetingService.getSummary(principal.getUser(), id);
    }
}
