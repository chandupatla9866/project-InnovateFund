package com.innovfund.report.repository;

import com.innovfund.report.entity.Report;
import com.innovfund.report.entity.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {
    List<Report> findAllByStatusOrderByCreatedAtDesc(ReportStatus status);
}
