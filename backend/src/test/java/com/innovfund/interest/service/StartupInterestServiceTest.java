package com.innovfund.interest.service;

import com.innovfund.ai.matching.MatchingService;
import com.innovfund.common.AccessDeniedCustomException;
import com.innovfund.interest.dto.FounderInterestedInvestorDto;
import com.innovfund.interest.entity.InterestStatus;
import com.innovfund.interest.entity.StartupInterest;
import com.innovfund.interest.repository.StartupInterestRepository;
import com.innovfund.investment.repository.InvestmentRepository;
import com.innovfund.investor.repository.InvestorProfileRepository;
import com.innovfund.notification.service.NotificationService;
import com.innovfund.startup.dto.StartupSummaryDto;
import com.innovfund.startup.entity.Startup;
import com.innovfund.startup.entity.StartupStage;
import com.innovfund.startup.repository.StartupRepository;
import com.innovfund.startup.service.StartupService;
import com.innovfund.user.entity.Role;
import com.innovfund.user.entity.User;
import com.innovfund.user.service.UserDisplayNameService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StartupInterestServiceTest {

    @Mock private StartupInterestRepository interestRepository;
    @Mock private StartupService startupService;
    @Mock private StartupRepository startupRepository;
    @Mock private InvestorProfileRepository investorProfileRepository;
    @Mock private InvestmentRepository investmentRepository;
    @Mock private MatchingService matchingService;
    @Mock private NotificationService notificationService;
    @Mock private UserDisplayNameService userDisplayNameService;
    @InjectMocks private StartupInterestService interestService;

    private User founder(UUID id) {
        return User.builder().id(id).email("founder@test.com").role(Role.FOUNDER).build();
    }

    private User investor(UUID id) {
        return User.builder().id(id).email("investor@test.com").role(Role.INVESTOR).build();
    }

    private Startup startup(User founder, boolean published) {
        return Startup.builder().id(UUID.randomUUID()).founder(founder).name("TestCo")
                .stage(StartupStage.MVP).published(published).build();
    }

    @Test
    void myInterests_includesStartupsThatHaveSinceBeenUnpublished() {
        User investor = investor(UUID.randomUUID());
        Startup unpublished = startup(founder(UUID.randomUUID()), false);
        StartupInterest interest = StartupInterest.builder().id(UUID.randomUUID()).investor(investor).startup(unpublished).build();

        when(interestRepository.findAllByInvestorIdOrderByCreatedAtDesc(investor.getId())).thenReturn(List.of(interest));
        when(startupService.toSummaryDto(unpublished)).thenReturn(
                new StartupSummaryDto(unpublished.getId(), "TestCo", null, null, StartupStage.MVP, null, null, false, false, 0));

        List<StartupSummaryDto> result = interestService.myInterests(investor);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).published()).isFalse();
    }

    @Test
    void accept_throwsWhenCallerDoesNotOwnStartup() {
        UUID actualFounderId = UUID.randomUUID();
        Startup s = startup(founder(actualFounderId), true);
        when(startupService.findOrThrow(s.getId())).thenReturn(s);

        User impostor = founder(UUID.randomUUID());

        assertThatThrownBy(() -> interestService.accept(impostor, s.getId(), UUID.randomUUID()))
                .isInstanceOf(AccessDeniedCustomException.class)
                .hasMessageContaining("do not own");
    }

    @Test
    void accept_setsStatusToAcceptedAndNotifiesInvestor() {
        UUID founderId = UUID.randomUUID();
        User founder = founder(founderId);
        Startup s = startup(founder, true);
        User investor = investor(UUID.randomUUID());
        StartupInterest interest = StartupInterest.builder().id(UUID.randomUUID())
                .investor(investor).startup(s).status(InterestStatus.PENDING).build();

        when(startupService.findOrThrow(s.getId())).thenReturn(s);
        when(interestRepository.findByInvestorIdAndStartupId(investor.getId(), s.getId())).thenReturn(Optional.of(interest));
        when(interestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        interestService.accept(founder, s.getId(), investor.getId());

        assertThat(interest.getStatus()).isEqualTo(InterestStatus.ACCEPTED);
    }

    @Test
    void hasAcceptedInterest_delegatesToRepository() {
        UUID investorId = UUID.randomUUID();
        UUID founderId = UUID.randomUUID();
        when(interestRepository.existsAcceptedBetween(investorId, founderId)).thenReturn(true);

        assertThat(interestService.hasAcceptedInterest(investorId, founderId)).isTrue();
    }

    @Test
    void listForFounder_aggregatesInterestsAcrossAllOfTheFoundersStartups() {
        User founder = founder(UUID.randomUUID());
        Startup alpha = startup(founder, true);
        Startup beta = startup(founder, true);
        User investorOne = investor(UUID.randomUUID());
        User investorTwo = investor(UUID.randomUUID());

        when(startupRepository.findAllByFounderIdOrderByCreatedAtDesc(founder.getId())).thenReturn(List.of(alpha, beta));
        when(interestRepository.findAllByStartupIdOrderByCreatedAtDesc(alpha.getId())).thenReturn(
                List.of(StartupInterest.builder().id(UUID.randomUUID()).investor(investorOne).startup(alpha)
                        .status(InterestStatus.PENDING).createdAt(java.time.Instant.now()).build()));
        when(interestRepository.findAllByStartupIdOrderByCreatedAtDesc(beta.getId())).thenReturn(
                List.of(StartupInterest.builder().id(UUID.randomUUID()).investor(investorTwo).startup(beta)
                        .status(InterestStatus.PENDING).createdAt(java.time.Instant.now()).build()));

        List<FounderInterestedInvestorDto> result = interestService.listForFounder(founder);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(FounderInterestedInvestorDto::startupId)
                .containsExactlyInAnyOrder(alpha.getId(), beta.getId());
    }
}
