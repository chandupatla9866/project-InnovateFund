package com.innovfund.feed.service;

import com.innovfund.feed.entity.SavedStartup;
import com.innovfund.feed.repository.SavedStartupRepository;
import com.innovfund.startup.dto.StartupSummaryDto;
import com.innovfund.startup.entity.Startup;
import com.innovfund.startup.entity.StartupStage;
import com.innovfund.startup.service.StartupService;
import com.innovfund.user.entity.Role;
import com.innovfund.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/** Same regression as FollowServiceTest, for the saved-startups list. */
@ExtendWith(MockitoExtension.class)
class SavedStartupServiceTest {

    @Mock private SavedStartupRepository savedStartupRepository;
    @Mock private StartupService startupService;
    @InjectMocks private SavedStartupService savedStartupService;

    private User investor() {
        return User.builder().id(UUID.randomUUID()).email("investor@test.com").role(Role.INVESTOR).build();
    }

    @Test
    void mySaved_includesStartupsThatHaveSinceBeenUnpublished() {
        User investor = investor();
        User founder = User.builder().id(UUID.randomUUID()).role(Role.FOUNDER).email("f@test.com").build();
        Startup unpublished = Startup.builder().id(UUID.randomUUID()).founder(founder).name("DraftCo")
                .stage(StartupStage.IDEA).published(false).build();
        SavedStartup saved = SavedStartup.builder().id(UUID.randomUUID()).user(investor).startup(unpublished).build();

        when(savedStartupRepository.findAllByUserIdOrderByCreatedAtDesc(investor.getId())).thenReturn(List.of(saved));
        when(startupService.toSummaryDto(unpublished)).thenReturn(
                new StartupSummaryDto(unpublished.getId(), "DraftCo", null, null, StartupStage.IDEA, null, null, false, false, 0));

        List<StartupSummaryDto> result = savedStartupService.mySaved(investor);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).published()).isFalse();
    }

    @Test
    void save_isIdempotentWhenAlreadySaved() {
        User investor = investor();
        UUID startupId = UUID.randomUUID();
        when(savedStartupRepository.findByStartupIdAndUserId(startupId, investor.getId()))
                .thenReturn(Optional.of(SavedStartup.builder().build()));

        savedStartupService.save(investor, startupId);

        org.mockito.Mockito.verifyNoInteractions(startupService);
        org.mockito.Mockito.verify(savedStartupRepository, org.mockito.Mockito.never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void isSaved_returnsFalseForNullInvestor() {
        assertThat(savedStartupService.isSaved(null, UUID.randomUUID())).isFalse();
    }
}
