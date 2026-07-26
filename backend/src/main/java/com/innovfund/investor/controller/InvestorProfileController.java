package com.innovfund.investor.controller;

import com.innovfund.investor.dto.FeaturedInvestorDto;
import com.innovfund.investor.dto.InvestorProfileDto;
import com.innovfund.investor.dto.UpdateInvestorProfileRequest;
import com.innovfund.investor.service.InvestorProfileService;
import com.innovfund.security.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/investors")
@RequiredArgsConstructor
public class InvestorProfileController {

    private final InvestorProfileService investorProfileService;

    @GetMapping("/featured")
    public List<FeaturedInvestorDto> featured(@RequestParam(defaultValue = "6") int limit) {
        return investorProfileService.featured(limit);
    }

    @GetMapping("/me")
    public InvestorProfileDto me(@AuthenticationPrincipal SecurityUser principal) {
        return investorProfileService.getByUser(principal.getUser());
    }

    @PutMapping("/me")
    public InvestorProfileDto updateMe(@AuthenticationPrincipal SecurityUser principal,
                                        @Valid @RequestBody UpdateInvestorProfileRequest request) {
        return investorProfileService.update(principal.getUser(), request);
    }
}
