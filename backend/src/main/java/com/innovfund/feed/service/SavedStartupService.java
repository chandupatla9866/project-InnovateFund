package com.innovfund.feed.service;

import com.innovfund.feed.entity.SavedStartup;
import com.innovfund.feed.repository.SavedStartupRepository;
import com.innovfund.startup.dto.StartupSummaryDto;
import com.innovfund.startup.entity.Startup;
import com.innovfund.startup.service.StartupService;
import com.innovfund.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SavedStartupService {

    private final SavedStartupRepository savedStartupRepository;
    private final StartupService startupService;

    @Transactional
    public void save(User investor, UUID startupId) {
        if (savedStartupRepository.findByStartupIdAndUserId(startupId, investor.getId()).isPresent()) {
            return;
        }
        Startup startup = startupService.findOrThrow(startupId);
        savedStartupRepository.save(SavedStartup.builder().startup(startup).user(investor).build());
    }

    @Transactional
    public void unsave(User investor, UUID startupId) {
        savedStartupRepository.deleteByStartupIdAndUserId(startupId, investor.getId());
    }

    public boolean isSaved(User investor, UUID startupId) {
        return investor != null && savedStartupRepository.findByStartupIdAndUserId(startupId, investor.getId()).isPresent();
    }

    @Transactional(readOnly = true)
    public List<StartupSummaryDto> mySaved(User investor) {
        // Same reasoning as FollowService.following(): a startup going back to draft after
        // being saved shouldn't 403 the investor's entire saved-list.
        return savedStartupRepository.findAllByUserIdOrderByCreatedAtDesc(investor.getId()).stream()
                .map(s -> startupService.toSummaryDto(s.getStartup()))
                .toList();
    }
}
