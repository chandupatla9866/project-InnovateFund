package com.innovfund.investment.controller;

import com.innovfund.investment.dto.InvestmentDto;
import com.innovfund.investment.dto.RecordInvestmentRequest;
import com.innovfund.investment.service.InvestmentService;
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
@RequiredArgsConstructor
public class InvestmentController {

    private final InvestmentService investmentService;

    @PostMapping("/api/startups/{startupId}/investments")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<InvestmentDto> record(@AuthenticationPrincipal SecurityUser principal,
                                                 @PathVariable UUID startupId,
                                                 @Valid @RequestBody RecordInvestmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(investmentService.record(principal.getUser(), startupId, request));
    }

    @GetMapping("/api/startups/{startupId}/investments")
    @PreAuthorize("hasRole('FOUNDER')")
    public List<InvestmentDto> forStartup(@AuthenticationPrincipal SecurityUser principal, @PathVariable UUID startupId) {
        return investmentService.forStartup(principal.getUser(), startupId);
    }

    @GetMapping("/api/investors/me/portfolio")
    @PreAuthorize("hasRole('INVESTOR')")
    public List<InvestmentDto> myPortfolio(@AuthenticationPrincipal SecurityUser principal) {
        return investmentService.myPortfolio(principal.getUser());
    }

}
