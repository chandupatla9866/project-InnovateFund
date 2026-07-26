package com.innovfund.ai.matching;

import com.innovfund.investor.entity.InvestorProfile;
import com.innovfund.investor.repository.InvestorProfileRepository;
import com.innovfund.notification.entity.NotificationType;
import com.innovfund.notification.repository.NotificationRepository;
import com.innovfund.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Every 6 hours, pushes a "Recommended Startup" notification to investors whose top TF-IDF match
 * scores at least 70% — once per startup per investor (deduped by notification link), so it never
 * repeats the same recommendation.
 */
@Component
@RequiredArgsConstructor
public class RecommendedStartupJob {

    private static final double MIN_MATCH_PERCENT = 70.0;

    private final InvestorProfileRepository investorProfileRepository;
    private final MatchingService matchingService;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    @Scheduled(fixedRate = 6, timeUnit = TimeUnit.HOURS)
    @Transactional
    public void pushTopRecommendations() {
        List<InvestorProfile> investors = investorProfileRepository.findAll();
        for (InvestorProfile investor : investors) {
            if (investor.getInvestmentInterests() == null || investor.getInvestmentInterests().isBlank()) {
                continue;
            }
            List<MatchDto> matches = matchingService.matchStartupsForInvestor(investor.getUser(), investor);
            if (matches.isEmpty()) {
                continue;
            }
            MatchDto top = matches.get(0);
            if (top.matchPercent() < MIN_MATCH_PERCENT) {
                continue;
            }
            String link = "/startups/" + top.id();
            if (notificationRepository.existsByRecipientIdAndLink(investor.getUser().getId(), link)) {
                continue;
            }
            notificationService.notify(investor.getUser(), NotificationType.AI_RECOMMENDATION,
                    top.name() + " is a " + Math.round(top.matchPercent()) + "% match for your investment interests",
                    link);
        }
    }
}
