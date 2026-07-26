package com.innovfund.feed.service;

import com.innovfund.feed.entity.Follow;
import com.innovfund.feed.repository.FollowRepository;
import com.innovfund.notification.service.NotificationService;
import com.innovfund.startup.entity.Startup;
import com.innovfund.startup.service.StartupService;
import com.innovfund.startup.dto.StartupSummaryDto;
import com.innovfund.startup.entity.StartupStage;
import com.innovfund.user.entity.Role;
import com.innovfund.user.entity.User;
import com.innovfund.user.service.UserDisplayNameService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Regression coverage for the bug fixed this session: following() used to build each row via
 * startupService.getById(), which throws AccessDeniedCustomException for an unpublished startup —
 * meaning one founder taking a followed startup back to draft crashed the investor's ENTIRE
 * following list, not just that one row. It must now build rows via toSummaryDto() instead, which
 * has no such check.
 */
@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock private FollowRepository followRepository;
    @Mock private StartupService startupService;
    @Mock private NotificationService notificationService;
    @Mock private UserDisplayNameService userDisplayNameService;
    @InjectMocks private FollowService followService;

    private User investor() {
        return User.builder().id(UUID.randomUUID()).email("investor@test.com").role(Role.INVESTOR).build();
    }

    private Startup startup(boolean published) {
        User founder = User.builder().id(UUID.randomUUID()).email("founder@test.com").role(Role.FOUNDER).build();
        return Startup.builder().id(UUID.randomUUID()).founder(founder).name("TestCo")
                .stage(StartupStage.MVP).published(published).build();
    }

    @Test
    void following_includesStartupsThatHaveSinceBeenUnpublished() {
        User investor = investor();
        Startup unpublished = startup(false);
        Follow follow = Follow.builder().id(UUID.randomUUID()).investor(investor).startup(unpublished).build();

        when(followRepository.findAllByInvestorIdOrderByCreatedAtDesc(investor.getId())).thenReturn(List.of(follow));
        when(startupService.toSummaryDto(unpublished)).thenReturn(
                new StartupSummaryDto(unpublished.getId(), "TestCo", null, null, StartupStage.MVP, null, null, false, false, 0));

        List<StartupSummaryDto> result = followService.following(investor);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).published()).isFalse();
    }

    @Test
    void following_returnsEmptyListWhenNothingFollowed() {
        User investor = investor();
        when(followRepository.findAllByInvestorIdOrderByCreatedAtDesc(investor.getId())).thenReturn(List.of());

        assertThat(followService.following(investor)).isEmpty();
    }

    @Test
    void follow_isIdempotentWhenAlreadyFollowing() {
        User investor = investor();
        Startup s = startup(true);
        when(followRepository.findByInvestorIdAndStartupId(investor.getId(), s.getId()))
                .thenReturn(java.util.Optional.of(Follow.builder().build()));

        followService.follow(investor, s.getId());

        // Already-following short-circuits before touching startupService/notificationService/save.
        org.mockito.Mockito.verifyNoInteractions(startupService, notificationService);
        org.mockito.Mockito.verify(followRepository, org.mockito.Mockito.never()).save(org.mockito.ArgumentMatchers.any());
    }
}
