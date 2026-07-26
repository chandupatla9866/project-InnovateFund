package com.innovfund.admin.controller;

import com.innovfund.admin.service.AdminService;
import com.innovfund.founder.dto.FounderProfileDto;
import com.innovfund.investor.dto.InvestorProfileDto;
import com.innovfund.startup.dto.StartupSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/founders")
    public List<FounderProfileDto> founders(@RequestParam(defaultValue = "false") boolean verified) {
        return adminService.listFounders(verified);
    }

    @PatchMapping("/founders/{id}/verify")
    public FounderProfileDto verifyFounder(@PathVariable UUID id, @RequestParam(defaultValue = "true") boolean verified) {
        return adminService.verifyFounder(id, verified);
    }

    @GetMapping("/investors")
    public List<InvestorProfileDto> investors(@RequestParam(defaultValue = "false") boolean verified) {
        return adminService.listInvestors(verified);
    }

    @PatchMapping("/investors/{id}/verify")
    public InvestorProfileDto verifyInvestor(@PathVariable UUID id, @RequestParam(defaultValue = "true") boolean verified) {
        return adminService.verifyInvestor(id, verified);
    }

    @GetMapping("/startups")
    public List<StartupSummaryDto> startups(@RequestParam(defaultValue = "false") boolean verified) {
        return adminService.listStartups(verified);
    }

    @PatchMapping("/startups/{id}/verify")
    public StartupSummaryDto verifyStartup(@PathVariable UUID id, @RequestParam(defaultValue = "true") boolean verified) {
        return adminService.verifyStartup(id, verified);
    }
}
