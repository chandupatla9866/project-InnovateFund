package com.innovfund.startup.service;

import com.innovfund.common.AccessDeniedCustomException;
import com.innovfund.feed.repository.FollowRepository;
import com.innovfund.interest.repository.StartupInterestRepository;
import com.innovfund.startup.dto.StartupDto;
import com.innovfund.startup.entity.Startup;
import com.innovfund.startup.repository.StartupRepository;
import com.innovfund.startup.search.NlSearchService;
import com.innovfund.user.entity.Role;
import com.innovfund.user.entity.User;
import com.innovfund.user.service.UserDisplayNameService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * getById()'s publish-visibility check is the rule that, when reused from a list-building
 * context (following/saved/interests), caused the whole-list 403 bug fixed this session — these
 * tests pin down exactly what that check should and shouldn't allow.
 */
@ExtendWith(MockitoExtension.class)
class StartupServiceTest {

    @Mock private StartupRepository startupRepository;
    @Mock private UserDisplayNameService userDisplayNameService;
    @Mock private StartupInterestRepository startupInterestRepository;
    @Mock private FollowRepository followRepository;
    @Mock private NlSearchService nlSearchService;
    @InjectMocks private StartupService startupService;

    private User founder(UUID id) {
        return User.builder().id(id).email("founder@test.com").role(Role.FOUNDER).build();
    }

    private User investor(UUID id) {
        return User.builder().id(id).email("investor@test.com").role(Role.INVESTOR).build();
    }

    private User admin(UUID id) {
        return User.builder().id(id).email("admin@test.com").role(Role.ADMIN).build();
    }

    private Startup startup(UUID founderId, boolean published) {
        return Startup.builder().id(UUID.randomUUID()).founder(founder(founderId)).name("TestCo").published(published).build();
    }

    @Test
    void getById_throwsForUnpublishedStartupViewedByStranger() {
        Startup s = startup(UUID.randomUUID(), false);
        when(startupRepository.findById(s.getId())).thenReturn(Optional.of(s));

        assertThatThrownBy(() -> startupService.getById(investor(UUID.randomUUID()), s.getId()))
                .isInstanceOf(AccessDeniedCustomException.class)
                .hasMessageContaining("not published");
    }

    @Test
    void getById_succeedsForUnpublishedStartupViewedByOwner() {
        UUID founderId = UUID.randomUUID();
        Startup s = startup(founderId, false);
        when(startupRepository.findById(s.getId())).thenReturn(Optional.of(s));

        StartupDto dto = startupService.getById(founder(founderId), s.getId());

        assertThat(dto.id()).isEqualTo(s.getId());
    }

    @Test
    void getById_succeedsForUnpublishedStartupViewedByAdmin() {
        Startup s = startup(UUID.randomUUID(), false);
        when(startupRepository.findById(s.getId())).thenReturn(Optional.of(s));

        StartupDto dto = startupService.getById(admin(UUID.randomUUID()), s.getId());

        assertThat(dto.id()).isEqualTo(s.getId());
    }

    @Test
    void getById_succeedsForPublishedStartupViewedByAnyone() {
        Startup s = startup(UUID.randomUUID(), true);
        when(startupRepository.findById(s.getId())).thenReturn(Optional.of(s));

        StartupDto dto = startupService.getById(investor(UUID.randomUUID()), s.getId());

        assertThat(dto.id()).isEqualTo(s.getId());
    }

    @Test
    void toSummaryDto_neverThrowsRegardlessOfPublishState() {
        // This is the actual fix: list-building must go through toSummaryDto (entity -> DTO,
        // no visibility check), never getById (which enforces visibility and throws).
        Startup unpublished = startup(UUID.randomUUID(), false);

        var summary = startupService.toSummaryDto(unpublished);

        assertThat(summary.id()).isEqualTo(unpublished.getId());
        assertThat(summary.published()).isFalse();
    }
}
