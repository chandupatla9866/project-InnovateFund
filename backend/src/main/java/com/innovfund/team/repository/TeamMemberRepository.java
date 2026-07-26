package com.innovfund.team.repository;

import com.innovfund.team.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID> {
    List<TeamMember> findAllByStartupIdOrderByDisplayOrderAscCreatedAtAsc(UUID startupId);
}
