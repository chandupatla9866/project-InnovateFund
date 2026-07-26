package com.innovfund.report.service;

import com.innovfund.common.ResourceNotFoundException;
import com.innovfund.report.dto.CreateReportRequest;
import com.innovfund.report.dto.ReportDto;
import com.innovfund.report.entity.Report;
import com.innovfund.report.entity.ReportStatus;
import com.innovfund.report.repository.ReportRepository;
import com.innovfund.user.entity.User;
import com.innovfund.user.service.UserDisplayNameService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserDisplayNameService userDisplayNameService;

    @Transactional
    public ReportDto submit(User reporter, CreateReportRequest request) {
        Report report = Report.builder()
                .reporter(reporter)
                .targetType(request.targetType())
                .targetId(request.targetId())
                .reason(request.reason())
                .build();
        return toDto(reportRepository.save(report));
    }

    @Transactional(readOnly = true)
    public List<ReportDto> list(ReportStatus status) {
        return reportRepository.findAllByStatusOrderByCreatedAtDesc(status).stream().map(this::toDto).toList();
    }

    @Transactional
    public ReportDto resolve(UUID id) {
        return updateStatus(id, ReportStatus.RESOLVED);
    }

    @Transactional
    public ReportDto dismiss(UUID id) {
        return updateStatus(id, ReportStatus.DISMISSED);
    }

    private ReportDto updateStatus(UUID id, ReportStatus status) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
        report.setStatus(status);
        return toDto(reportRepository.save(report));
    }

    private ReportDto toDto(Report r) {
        return new ReportDto(r.getId(), r.getReporter().getId(), userDisplayNameService.resolveFullName(r.getReporter()),
                r.getTargetType(), r.getTargetId(), r.getReason(), r.getStatus(), r.getCreatedAt());
    }
}
