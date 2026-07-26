package com.innovfund.feed.service;

import com.innovfund.feed.entity.Follow;
import com.innovfund.feed.repository.FollowRepository;
import com.innovfund.notification.entity.NotificationType;
import com.innovfund.notification.service.NotificationService;
import com.innovfund.startup.dto.StartupSummaryDto;
import com.innovfund.startup.entity.Startup;
import com.innovfund.startup.service.StartupService;
import com.innovfund.user.entity.User;
import com.innovfund.user.service.UserDisplayNameService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final StartupService startupService;
    private final NotificationService notificationService;
    private final UserDisplayNameService userDisplayNameService;

    @Transactional
    public void follow(User investor, UUID startupId) {
        if (followRepository.findByInvestorIdAndStartupId(investor.getId(), startupId).isPresent()) {
            return;
        }
        Startup startup = startupService.findOrThrow(startupId);
        followRepository.save(Follow.builder().investor(investor).startup(startup).build());
        notificationService.notify(startup.getFounder(), NotificationType.INVESTOR_FOLLOWED,
                userDisplayNameService.resolveFullName(investor) + " is now following " + startup.getName(),
                "/startups/" + startup.getId());
    }

    @Transactional
    public void unfollow(User investor, UUID startupId) {
        followRepository.deleteByInvestorIdAndStartupId(investor.getId(), startupId);
    }

    public long followerCount(UUID startupId) {
        return followRepository.countByStartupId(startupId);
    }

    @Transactional(readOnly = true)
    public List<StartupSummaryDto> following(User investor) {
        // Build summaries directly from the followed entity — a startup the founder later
        // unpublishes should still show up in the investor's own following list, not
        // 403 the whole endpoint the way startupService.getById()'s visibility check would.
        return followRepository.findAllByInvestorIdOrderByCreatedAtDesc(investor.getId()).stream()
                .map(f -> startupService.toSummaryDto(f.getStartup()))
                .toList();
    }
}
