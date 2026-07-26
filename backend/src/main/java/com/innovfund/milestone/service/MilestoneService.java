package com.innovfund.milestone.service;

import com.innovfund.common.AccessDeniedCustomException;
import com.innovfund.common.ResourceNotFoundException;
import com.innovfund.feed.dto.CreatePostRequest;
import com.innovfund.feed.entity.PostType;
import com.innovfund.feed.service.PostService;
import com.innovfund.milestone.dto.CreateMilestoneRequest;
import com.innovfund.milestone.dto.MilestoneDto;
import com.innovfund.milestone.dto.UpdateMilestoneRequest;
import com.innovfund.milestone.entity.Milestone;
import com.innovfund.milestone.repository.MilestoneRepository;
import com.innovfund.startup.entity.Startup;
import com.innovfund.startup.service.StartupService;
import com.innovfund.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MilestoneService {

    private final MilestoneRepository milestoneRepository;
    private final StartupService startupService;
    private final PostService postService;

    @Transactional
    public MilestoneDto create(User founder, UUID startupId, CreateMilestoneRequest request) {
        Startup startup = getOwnedStartup(founder, startupId);
        Milestone milestone = Milestone.builder()
                .startup(startup)
                .title(request.title())
                .description(request.description())
                .targetDate(request.targetDate())
                .build();
        return toDto(milestoneRepository.save(milestone));
    }

    @Transactional(readOnly = true)
    public List<MilestoneDto> listForStartup(UUID startupId) {
        return milestoneRepository.findAllByStartupIdOrderByTargetDateAscCreatedAtAsc(startupId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public MilestoneDto update(User founder, UUID milestoneId, UpdateMilestoneRequest request) {
        Milestone milestone = findOwned(founder, milestoneId);
        milestone.setTitle(request.title());
        milestone.setDescription(request.description());
        milestone.setTargetDate(request.targetDate());
        return toDto(milestoneRepository.save(milestone));
    }

    /** Toggling a milestone to completed also publishes a MILESTONE feed post announcing it. */
    @Transactional
    public MilestoneDto toggleComplete(User founder, UUID milestoneId) {
        Milestone milestone = findOwned(founder, milestoneId);
        boolean nowCompleted = !milestone.isCompleted();
        milestone.setCompleted(nowCompleted);
        milestone.setCompletedAt(nowCompleted ? Instant.now() : null);
        Milestone saved = milestoneRepository.save(milestone);

        if (nowCompleted) {
            String text = "Milestone reached: " + milestone.getTitle()
                    + (milestone.getDescription() != null && !milestone.getDescription().isBlank()
                            ? " — " + milestone.getDescription() : "");
            postService.create(founder, new CreatePostRequest(milestone.getStartup().getId(), PostType.MILESTONE, text, null));
        }
        return toDto(saved);
    }

    @Transactional
    public void delete(User founder, UUID milestoneId) {
        milestoneRepository.delete(findOwned(founder, milestoneId));
    }

    private Startup getOwnedStartup(User founder, UUID startupId) {
        Startup startup = startupService.findOrThrow(startupId);
        if (!startup.getFounder().getId().equals(founder.getId())) {
            throw new AccessDeniedCustomException("You do not own this startup");
        }
        return startup;
    }

    private Milestone findOwned(User founder, UUID milestoneId) {
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found"));
        if (!milestone.getStartup().getFounder().getId().equals(founder.getId())) {
            throw new AccessDeniedCustomException("You do not own this milestone");
        }
        return milestone;
    }

    private MilestoneDto toDto(Milestone m) {
        return new MilestoneDto(m.getId(), m.getStartup().getId(), m.getTitle(), m.getDescription(),
                m.getTargetDate(), m.isCompleted(), m.getCompletedAt(), m.getCreatedAt());
    }
}
