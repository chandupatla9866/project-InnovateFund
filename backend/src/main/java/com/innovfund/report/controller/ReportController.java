package com.innovfund.report.controller;

import com.innovfund.report.dto.CreateReportRequest;
import com.innovfund.report.dto.ReportDto;
import com.innovfund.report.entity.ReportStatus;
import com.innovfund.report.service.ReportService;
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
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/api/reports")
    public ResponseEntity<ReportDto> submit(@AuthenticationPrincipal SecurityUser principal,
                                             @Valid @RequestBody CreateReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.submit(principal.getUser(), request));
    }

    @GetMapping("/api/admin/reports")
    public List<ReportDto> list(@RequestParam(defaultValue = "PENDING") ReportStatus status) {
        return reportService.list(status);
    }

    @PatchMapping("/api/admin/reports/{id}/resolve")
    public ReportDto resolve(@PathVariable UUID id) {
        return reportService.resolve(id);
    }

    @PatchMapping("/api/admin/reports/{id}/dismiss")
    public ReportDto dismiss(@PathVariable UUID id) {
        return reportService.dismiss(id);
    }
}
