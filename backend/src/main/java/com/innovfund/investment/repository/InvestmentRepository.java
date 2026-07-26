package com.innovfund.investment.repository;

import com.innovfund.investment.entity.Investment;
import com.innovfund.investment.entity.InvestmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InvestmentRepository extends JpaRepository<Investment, UUID> {
    List<Investment> findAllByStartupIdOrderByCreatedAtDesc(UUID startupId);
    List<Investment> findAllByInvestorIdOrderByCreatedAtDesc(UUID investorId);
    List<Investment> findAllByStatus(InvestmentStatus status);
}
