package com.innovfund.interest.service;

import com.innovfund.ai.matching.MatchingService;
import com.innovfund.common.AccessDeniedCustomException;
import com.innovfund.common.ResourceNotFoundException;
import com.innovfund.interest.dto.FounderInterestedInvestorDto;
import com.innovfund.interest.dto.InterestedInvestorDto;
import com.innovfund.interest.entity.InterestStatus;
import com.innovfund.interest.entity.StartupInterest;
import com.innovfund.interest.repository.StartupInterestRepository;
import com.innovfund.investment.repository.InvestmentRepository;
import com.innovfund.investor.entity.InvestorProfile;
import com.innovfund.investor.repository.InvestorProfileRepository;
import com.innovfund.notification.entity.NotificationType;
import com.innovfund.notification.service.NotificationService;
import com.innovfund.startup.dto.StartupSummaryDto;
import com.innovfund.startup.entity.Startup;
import com.innovfund.startup.repository.StartupRepository;
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
public class StartupInterestService {

    private final StartupInterestRepository interestRepository;
    private final StartupService startupService;
    private final StartupRepository startupRepository;
    private final InvestorProfileRepository investorProfileRepository;
    private final InvestmentRepository investmentRepository;
    private final MatchingService matchingService;
    private final NotificationService notificationService;
    private final UserDisplayNameService userDisplayNameService;

    @Transactional
    public void expressInterest(User investor, UUID startupId) {
        if (interestRepository.findByInvestorIdAndStartupId(investor.getId(), startupId).isPresent()) {
            return;
        }
        Startup startup = startupService.findOrThrow(startupId);
        interestRepository.save(StartupInterest.builder().investor(investor).startup(startup).build());
        notificationService.notify(startup.getFounder(), NotificationType.INVESTOR_INTERESTED,
                userDisplayNameService.resolveFullName(investor) + " is interested in investing in " + startup.getName(),
                "/startups/" + startup.getId() + "#interested-investors");
    }

    @Transactional
    public void withdrawInterest(User investor, UUID startupId) {
        interestRepository.deleteByInvestorIdAndStartupId(investor.getId(), startupId);
    }

    public long countForStartup(UUID startupId) {
        return interestRepository.countByStartupId(startupId);
    }

    /** Founder accepts an investor's interest — this is what opens chat between them. */
    @Transactional
    public void accept(User founder, UUID startupId, UUID investorId) {
        StartupInterest interest = findOwned(founder, startupId, investorId);
        interest.setStatus(InterestStatus.ACCEPTED);
        interestRepository.save(interest);
        notificationService.notify(interest.getInvestor(), NotificationType.INTEREST_ACCEPTED,
                userDisplayNameService.resolveFullName(founder) + " accepted your interest in "
                        + interest.getStartup().getName() + " — you can now chat",
                "/chat/" + founder.getId());
    }

    @Transactional
    public void reject(User founder, UUID startupId, UUID investorId) {
        StartupInterest interest = findOwned(founder, startupId, investorId);
        interest.setStatus(InterestStatus.REJECTED);
        interestRepository.save(interest);
        notificationService.notify(interest.getInvestor(), NotificationType.INTEREST_REJECTED,
                "Your interest in " + interest.getStartup().getName() + " was declined", null);
    }

    /** Used by ChatService to gate messaging between a founder and an investor. */
    public boolean hasAcceptedInterest(UUID investorId, UUID founderId) {
        return interestRepository.existsAcceptedBetween(investorId, founderId);
    }

    private StartupInterest findOwned(User founder, UUID startupId, UUID investorId) {
        Startup startup = startupService.findOrThrow(startupId);
        if (!startup.getFounder().getId().equals(founder.getId())) {
            throw new AccessDeniedCustomException("You do not own this startup");
        }
        return interestRepository.findByInvestorIdAndStartupId(investorId, startupId)
                .orElseThrow(() -> new ResourceNotFoundException("No interest record found for this investor"));
    }

    @Transactional(readOnly = true)
    public List<InterestedInvestorDto> listForStartup(User founder, UUID startupId) {
        Startup startup = startupService.findOrThrow(startupId);
        if (!startup.getFounder().getId().equals(founder.getId())) {
            throw new AccessDeniedCustomException("You do not own this startup");
        }
        return interestRepository.findAllByStartupIdOrderByCreatedAtDesc(startupId).stream()
                .map(i -> {
                    UUID investorId = i.getInvestor().getId();
                    InvestorProfile profile = investorProfileRepository.findByUserId(investorId).orElse(null);
                    long pastInvestments = investmentRepository.findAllByInvestorIdOrderByCreatedAtDesc(investorId).size();
                    Double matchPercent = matchingService.matchPercentFor(startup, investorId);
                    return new InterestedInvestorDto(
                            investorId,
                            userDisplayNameService.resolveFullName(i.getInvestor()),
                            profile != null ? profile.getFirmName() : null,
                            profile != null ? profile.getInvestmentInterests() : null,
                            profile != null && profile.isVerified(),
                            pastInvestments,
                            matchPercent,
                            i.getStatus().name(),
                            i.getCreatedAt());
                })
                .toList();
    }

    /** Aggregates interested investors across every startup the founder owns, for a single consolidated view. */
    @Transactional(readOnly = true)
    public List<FounderInterestedInvestorDto> listForFounder(User founder) {
        List<Startup> startups = startupRepository.findAllByFounderIdOrderByCreatedAtDesc(founder.getId());
        return startups.stream()
                .flatMap(startup -> interestRepository.findAllByStartupIdOrderByCreatedAtDesc(startup.getId()).stream()
                        .map(i -> {
                            UUID investorId = i.getInvestor().getId();
                            InvestorProfile profile = investorProfileRepository.findByUserId(investorId).orElse(null);
                            long pastInvestments = investmentRepository.findAllByInvestorIdOrderByCreatedAtDesc(investorId).size();
                            Double matchPercent = matchingService.matchPercentFor(startup, investorId);
                            return new FounderInterestedInvestorDto(
                                    startup.getId(),
                                    startup.getName(),
                                    investorId,
                                    userDisplayNameService.resolveFullName(i.getInvestor()),
                                    profile != null ? profile.getFirmName() : null,
                                    profile != null ? profile.getInvestmentInterests() : null,
                                    profile != null && profile.isVerified(),
                                    pastInvestments,
                                    matchPercent,
                                    i.getStatus().name(),
                                    i.getCreatedAt());
                        }))
                .sorted((a, b) -> b.expressedAt().compareTo(a.expressedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StartupSummaryDto> myInterests(User investor) {
        // Same fix as FollowService.following()/SavedStartupService.mySaved(): don't let
        // startupService.getById()'s publish-visibility check 403 the whole list.
        return interestRepository.findAllByInvestorIdOrderByCreatedAtDesc(investor.getId()).stream()
                .map(i -> startupService.toSummaryDto(i.getStartup()))
                .toList();
    }
}
