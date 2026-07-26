package com.innovfund.feed.repository;

import com.innovfund.feed.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, UUID> {
    Optional<Follow> findByInvestorIdAndStartupId(UUID investorId, UUID startupId);
    long countByStartupId(UUID startupId);
    List<Follow> findAllByInvestorIdOrderByCreatedAtDesc(UUID investorId);
    void deleteByInvestorIdAndStartupId(UUID investorId, UUID startupId);
}
