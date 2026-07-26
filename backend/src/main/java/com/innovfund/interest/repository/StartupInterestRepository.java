package com.innovfund.interest.repository;

import com.innovfund.interest.entity.StartupInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StartupInterestRepository extends JpaRepository<StartupInterest, UUID> {
    Optional<StartupInterest> findByInvestorIdAndStartupId(UUID investorId, UUID startupId);
    long countByStartupId(UUID startupId);
    List<StartupInterest> findAllByStartupIdOrderByCreatedAtDesc(UUID startupId);
    List<StartupInterest> findAllByInvestorIdOrderByCreatedAtDesc(UUID investorId);
    void deleteByInvestorIdAndStartupId(UUID investorId, UUID startupId);

    @Query("""
            select case when count(si) > 0 then true else false end from StartupInterest si
            where si.investor.id = :investorId and si.startup.founder.id = :founderId and si.status = 'ACCEPTED'
            """)
    boolean existsAcceptedBetween(@Param("investorId") UUID investorId, @Param("founderId") UUID founderId);
}
