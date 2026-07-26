package com.innovfund.milestone.repository;

import com.innovfund.milestone.entity.Milestone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MilestoneRepository extends JpaRepository<Milestone, UUID> {
    List<Milestone> findAllByStartupIdOrderByTargetDateAscCreatedAtAsc(UUID startupId);
}
