package com.innovfund.analytics.service;

import com.innovfund.analytics.dto.FounderAnalyticsDto;
import com.innovfund.analytics.dto.InvestorAnalyticsDto;
import com.innovfund.analytics.dto.PlatformAnalyticsDto;
import com.innovfund.analytics.dto.StartupAnalyticsDto;
import com.innovfund.ai.entity.AiReport;
import com.innovfund.ai.repository.AiReportRepository;
import com.innovfund.chat.repository.ChatMessageRepository;
import com.innovfund.feed.repository.FollowRepository;
import com.innovfund.feed.repository.LikeRepository;
import com.innovfund.feed.repository.PostRepository;
import com.innovfund.founder.repository.FounderProfileRepository;
import com.innovfund.interest.repository.StartupInterestRepository;
import com.innovfund.investment.entity.Investment;
import com.innovfund.investment.repository.InvestmentRepository;
import com.innovfund.investor.repository.InvestorProfileRepository;
import com.innovfund.meeting.entity.Meeting;
import com.innovfund.meeting.entity.MeetingStatus;
import com.innovfund.meeting.repository.MeetingRepository;
import com.innovfund.report.entity.ReportStatus;
import com.innovfund.report.repository.ReportRepository;
import com.innovfund.startup.entity.Startup;
import com.innovfund.startup.repository.StartupRepository;
import com.innovfund.user.entity.Role;
import com.innovfund.user.entity.User;
import com.innovfund.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private static final Set<MeetingStatus> UPCOMING_STATUSES = Set.of(MeetingStatus.PENDING, MeetingStatus.ACCEPTED);

    private final StartupRepository startupRepository;
    private final FollowRepository followRepository;
    private final LikeRepository likeRepository;
    private final StartupInterestRepository startupInterestRepository;
    private final AiReportRepository aiReportRepository;
    private final MeetingRepository meetingRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final InvestmentRepository investmentRepository;
    private final PostRepository postRepository;
    private final FounderProfileRepository founderProfileRepository;
    private final InvestorProfileRepository investorProfileRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public FounderAnalyticsDto founderAnalytics(User founder) {
        List<Startup> startups = startupRepository.findAllByFounderIdOrderByCreatedAtDesc(founder.getId());

        List<StartupAnalyticsDto> startupDtos = startups.stream().map(this::toStartupAnalytics).toList();

        long totalViews = startupDtos.stream().mapToLong(StartupAnalyticsDto::viewCount).sum();
        long totalFollowers = startupDtos.stream().mapToLong(StartupAnalyticsDto::followerCount).sum();
        long totalLikes = startupDtos.stream().mapToLong(StartupAnalyticsDto::likeCount).sum();
        long totalInterested = startupDtos.stream().mapToLong(StartupAnalyticsDto::interestedInvestorsCount).sum();
        BigDecimal totalGoal = startupDtos.stream().map(StartupAnalyticsDto::fundingGoal)
                .filter(java.util.Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalProgress = startupDtos.stream().map(StartupAnalyticsDto::fundingProgress)
                .filter(java.util.Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Meeting> meetings = meetingRepository.findAllForUser(founder.getId());
        long upcomingMeetings = countUpcoming(meetings);
        long unreadMessages = chatMessageRepository.countByRecipientIdAndReadFalse(founder.getId());

        return new FounderAnalyticsDto(totalViews, totalFollowers, totalLikes, totalInterested,
                totalGoal, totalProgress, meetings.size(), upcomingMeetings, unreadMessages, startupDtos);
    }

    @Transactional(readOnly = true)
    public InvestorAnalyticsDto investorAnalytics(User investorUser) {
        long followingCount = followRepository.findAllByInvestorIdOrderByCreatedAtDesc(investorUser.getId()).size();
        long interestsCount = startupInterestRepository.findAllByInvestorIdOrderByCreatedAtDesc(investorUser.getId()).size();

        List<Meeting> meetings = meetingRepository.findAllForUser(investorUser.getId());
        long upcomingMeetings = countUpcoming(meetings);
        long unreadMessages = chatMessageRepository.countByRecipientIdAndReadFalse(investorUser.getId());

        List<Investment> investments = investmentRepository.findAllByInvestorIdOrderByCreatedAtDesc(investorUser.getId());
        long portfolioStartupsCount = investments.stream().map(i -> i.getStartup().getId()).distinct().count();
        BigDecimal portfolioTotal = investments.stream().map(Investment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new InvestorAnalyticsDto(followingCount, interestsCount, meetings.size(), upcomingMeetings,
                unreadMessages, portfolioStartupsCount, portfolioTotal);
    }

    @Transactional(readOnly = true)
    public PlatformAnalyticsDto platformAnalytics() {
        long totalFounders = userRepository.countByRole(Role.FOUNDER);
        long totalInvestors = userRepository.countByRole(Role.INVESTOR);
        long totalStartups = startupRepository.count();
        long publishedStartups = startupRepository.findAllPublished().size();

        long pendingFounders = founderProfileRepository.findAllByVerified(false).size();
        long pendingInvestors = investorProfileRepository.findAllByVerified(false).size();
        long pendingStartups = startupRepository.findAllByVerified(false).size();

        BigDecimal totalInvestmentVolume = investmentRepository.findAll().stream()
                .map(Investment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        long pendingReports = reportRepository.findAllByStatusOrderByCreatedAtDesc(ReportStatus.PENDING).size();

        return new PlatformAnalyticsDto(
                userRepository.count(),
                totalFounders,
                totalInvestors,
                totalStartups,
                publishedStartups,
                pendingStartups,
                pendingFounders,
                pendingInvestors,
                postRepository.count(),
                investmentRepository.count(),
                totalInvestmentVolume,
                meetingRepository.count(),
                pendingReports
        );
    }

    private long countUpcoming(List<Meeting> meetings) {
        Instant now = Instant.now();
        return meetings.stream()
                .filter(m -> UPCOMING_STATUSES.contains(m.getStatus()) && m.getScheduledAt().isAfter(now))
                .count();
    }

    private StartupAnalyticsDto toStartupAnalytics(Startup startup) {
        UUID id = startup.getId();
        long followers = followRepository.countByStartupId(id);
        long likes = likeRepository.countByPostStartupId(id);
        long interested = startupInterestRepository.countByStartupId(id);
        Double latestAiScore = aiReportRepository.findFirstByStartupIdOrderByCreatedAtDesc(id)
                .map(AiReport::getOverallScore).orElse(null);

        return new StartupAnalyticsDto(id, startup.getName(), startup.getViewCount(), followers, likes,
                interested, startup.getFundingGoal(), startup.getFundingProgress(), latestAiScore);
    }
}
