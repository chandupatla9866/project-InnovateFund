package com.innovfund.diligence.controller;

import com.innovfund.diligence.dto.DueDiligenceDocumentDto;
import com.innovfund.diligence.dto.DueDiligenceRequestDto;
import com.innovfund.diligence.dto.UploadDocumentRequest;
import com.innovfund.diligence.service.DueDiligenceService;
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
@RequestMapping("/api/startups/{startupId}/due-diligence")
@RequiredArgsConstructor
public class DueDiligenceController {

    private final DueDiligenceService dueDiligenceService;

    @PostMapping("/request")
    @PreAuthorize("hasRole('INVESTOR')")
    public DueDiligenceRequestDto requestAccess(@AuthenticationPrincipal SecurityUser principal, @PathVariable UUID startupId) {
        return dueDiligenceService.requestAccess(principal.getUser(), startupId);
    }

    @GetMapping("/my-status")
    @PreAuthorize("hasRole('INVESTOR')")
    public ResponseEntity<DueDiligenceRequestDto> myStatus(@AuthenticationPrincipal SecurityUser principal, @PathVariable UUID startupId) {
        DueDiligenceRequestDto dto = dueDiligenceService.myStatus(principal.getUser(), startupId);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.noContent().build();
    }

    @GetMapping("/requests")
    @PreAuthorize("hasRole('FOUNDER')")
    public List<DueDiligenceRequestDto> requests(@AuthenticationPrincipal SecurityUser principal, @PathVariable UUID startupId) {
        return dueDiligenceService.listRequests(principal.getUser(), startupId);
    }

    @PatchMapping("/requests/{requestId}/approve")
    @PreAuthorize("hasRole('FOUNDER')")
    public DueDiligenceRequestDto approve(@AuthenticationPrincipal SecurityUser principal,
                                          @PathVariable UUID startupId, @PathVariable UUID requestId) {
        return dueDiligenceService.approve(principal.getUser(), requestId);
    }

    @PatchMapping("/requests/{requestId}/reject")
    @PreAuthorize("hasRole('FOUNDER')")
    public DueDiligenceRequestDto reject(@AuthenticationPrincipal SecurityUser principal,
                                         @PathVariable UUID startupId, @PathVariable UUID requestId) {
        return dueDiligenceService.reject(principal.getUser(), requestId);
    }

    @PostMapping("/documents")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<DueDiligenceDocumentDto> uploadDocument(@AuthenticationPrincipal SecurityUser principal,
                                                                   @PathVariable UUID startupId,
                                                                   @Valid @RequestBody UploadDocumentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dueDiligenceService.uploadDocument(principal.getUser(), startupId, request));
    }

    @GetMapping("/documents")
    public List<DueDiligenceDocumentDto> documents(@AuthenticationPrincipal SecurityUser principal, @PathVariable UUID startupId) {
        return dueDiligenceService.listDocuments(principal.getUser(), startupId);
    }

    @DeleteMapping("/documents/{documentId}")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<Void> deleteDocument(@AuthenticationPrincipal SecurityUser principal,
                                                @PathVariable UUID startupId, @PathVariable UUID documentId) {
        dueDiligenceService.deleteDocument(principal.getUser(), startupId, documentId);
        return ResponseEntity.noContent().build();
    }
}
