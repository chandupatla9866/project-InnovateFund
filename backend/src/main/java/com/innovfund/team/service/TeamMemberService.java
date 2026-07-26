package com.innovfund.team.service;

import com.innovfund.common.AccessDeniedCustomException;
import com.innovfund.common.ResourceNotFoundException;
import com.innovfund.startup.entity.Startup;
import com.innovfund.startup.service.StartupService;
import com.innovfund.team.dto.TeamMemberDto;
import com.innovfund.team.dto.TeamMemberRequest;
import com.innovfund.team.entity.TeamMember;
import com.innovfund.team.repository.TeamMemberRepository;
import com.innovfund.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamMemberService {

    private final TeamMemberRepository teamMemberRepository;
    private final StartupService startupService;

    @Transactional
    public TeamMemberDto create(User founder, UUID startupId, TeamMemberRequest request) {
        Startup startup = getOwnedStartup(founder, startupId);
        TeamMember member = TeamMember.builder()
                .startup(startup)
                .name(request.name())
                .role(request.role())
                .bio(request.bio())
                .photoUrl(request.photoUrl())
                .displayOrder(request.displayOrder())
                .build();
        return toDto(teamMemberRepository.save(member));
    }

    @Transactional
    public TeamMemberDto update(User founder, UUID memberId, TeamMemberRequest request) {
        TeamMember member = findOwned(founder, memberId);
        member.setName(request.name());
        member.setRole(request.role());
        member.setBio(request.bio());
        member.setPhotoUrl(request.photoUrl());
        member.setDisplayOrder(request.displayOrder());
        return toDto(teamMemberRepository.save(member));
    }

    @Transactional
    public void delete(User founder, UUID memberId) {
        teamMemberRepository.delete(findOwned(founder, memberId));
    }

    @Transactional(readOnly = true)
    public List<TeamMemberDto> listForStartup(UUID startupId) {
        return teamMemberRepository.findAllByStartupIdOrderByDisplayOrderAscCreatedAtAsc(startupId).stream()
                .map(this::toDto)
                .toList();
    }

    private Startup getOwnedStartup(User founder, UUID startupId) {
        Startup startup = startupService.findOrThrow(startupId);
        if (!startup.getFounder().getId().equals(founder.getId())) {
            throw new AccessDeniedCustomException("You do not own this startup");
        }
        return startup;
    }

    private TeamMember findOwned(User founder, UUID memberId) {
        TeamMember member = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Team member not found"));
        if (!member.getStartup().getFounder().getId().equals(founder.getId())) {
            throw new AccessDeniedCustomException("You do not own this startup");
        }
        return member;
    }

    private TeamMemberDto toDto(TeamMember m) {
        return new TeamMemberDto(m.getId(), m.getStartup().getId(), m.getName(), m.getRole(), m.getBio(),
                m.getPhotoUrl(), m.getDisplayOrder());
    }
}
