package com.innovfund.diligence.repository;

import com.innovfund.diligence.entity.DueDiligenceRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DueDiligenceRequestRepository extends JpaRepository<DueDiligenceRequest, UUID> {
    Optional<DueDiligenceRequest> findByInvestorIdAndStartupId(UUID investorId, UUID startupId);
    List<DueDiligenceRequest> findAllByStartupIdOrderByCreatedAtDesc(UUID startupId);
}
