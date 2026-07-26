package com.innovfund.ai.repository;

import com.innovfund.ai.entity.AiReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AiReportRepository extends JpaRepository<AiReport, UUID> {
    List<AiReport> findAllByStartupIdOrderByCreatedAtDesc(UUID startupId);
    Optional<AiReport> findFirstByStartupIdOrderByCreatedAtDesc(UUID startupId);
}
