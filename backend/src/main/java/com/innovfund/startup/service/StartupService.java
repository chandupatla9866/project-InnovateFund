package com.innovfund.startup.service;

import com.innovfund.common.AccessDeniedCustomException;
import com.innovfund.common.PageResponse;
import com.innovfund.common.ResourceNotFoundException;
import com.innovfund.feed.repository.FollowRepository;
import com.innovfund.interest.repository.StartupInterestRepository;
import com.innovfund.user.service.UserDisplayNameService;
import com.innovfund.startup.dto.CreateStartupRequest;
import com.innovfund.startup.dto.StartupDto;
import com.innovfund.startup.dto.StartupSummaryDto;
import com.innovfund.startup.dto.UpdateStartupRequest;
import com.innovfund.startup.entity.Startup;
import com.innovfund.startup.entity.StartupStage;
import com.innovfund.startup.repository.StartupRepository;
import com.innovfund.startup.search.NlSearchService;
import com.innovfund.startup.search.StartupSearchFilters;
import com.innovfund.user.entity.Role;
import com.innovfund.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StartupService {

    private final StartupRepository startupRepository;
    private final UserDisplayNameService userDisplayNameService;
    private final StartupInterestRepository startupInterestRepository;
    private final FollowRepository followRepository;
    private final NlSearchService nlSearchService;

    @Transactional
    public StartupDto create(User founder, CreateStartupRequest request) {
        Startup startup = Startup.builder()
                .founder(founder)
                .name(request.name())
                .logoUrl(request.logoUrl())
                .coverImageUrl(request.coverImageUrl())
                .industry(request.industry())
                .country(request.country())
                .stage(request.stage())
                .problem(request.problem())
                .solution(request.solution())
                .businessModel(request.businessModel())
                .revenueModel(request.revenueModel())
                .targetAudience(request.targetAudience())
                .market(request.market())
                .competitors(request.competitors())
                .fundingGoal(request.fundingGoal())
                .pitchDeckUrl(request.pitchDeckUrl())
                .demoVideoUrl(request.demoVideoUrl())
                .equityOffered(request.equityOffered())
                .websiteUrl(request.websiteUrl())
                .socialLinks(request.socialLinks())
                .published(false)
                .build();
        return toDto(startupRepository.save(startup));
    }

    @Transactional
    public StartupDto update(User founder, UUID startupId, UpdateStartupRequest request) {
        Startup startup = getOwned(founder, startupId);
        startup.setName(request.name());
        startup.setLogoUrl(request.logoUrl());
        startup.setCoverImageUrl(request.coverImageUrl());
        startup.setIndustry(request.industry());
        startup.setCountry(request.country());
        startup.setStage(request.stage());
        startup.setProblem(request.problem());
        startup.setSolution(request.solution());
        startup.setBusinessModel(request.businessModel());
        startup.setRevenueModel(request.revenueModel());
        startup.setTargetAudience(request.targetAudience());
        startup.setMarket(request.market());
        startup.setCompetitors(request.competitors());
        startup.setFundingGoal(request.fundingGoal());
        startup.setPitchDeckUrl(request.pitchDeckUrl());
        startup.setDemoVideoUrl(request.demoVideoUrl());
        startup.setEquityOffered(request.equityOffered());
        startup.setWebsiteUrl(request.websiteUrl());
        startup.setSocialLinks(request.socialLinks());
        return toDto(startupRepository.save(startup));
    }

    @Transactional(readOnly = true)
    public StartupDto getById(User viewer, UUID startupId) {
        Startup startup = findOrThrow(startupId);
        if (!startup.isPublished() && !isOwnerOrAdmin(viewer, startup)) {
            throw new AccessDeniedCustomException("This startup is not published yet");
        }
        return toDto(startup);
    }

    @Transactional(readOnly = true)
    public PageResponse<StartupSummaryDto> listPublished(String industry, StartupStage stage, String country,
                                                           java.math.BigDecimal minFunding, java.math.BigDecimal maxFunding,
                                                           Double minAiScore, Pageable pageable) {
        Page<Startup> page = startupRepository.searchPublished(industry, stage, country, minFunding, maxFunding, minAiScore, pageable);
        return PageResponse.from(page.map(this::toSummaryDto));
    }

    /**
     * Trending Score = views + followers*3 + interested-investors*5 + recency bonus (linearly
     * decaying over 14 days). Same explainable, non-AI formula style as the feed's post trending.
     */
    @Transactional(readOnly = true)
    public List<StartupSummaryDto> trending(int limit) {
        List<Startup> published = startupRepository.findAllPublished();
        return published.stream()
                .sorted(java.util.Comparator.comparingDouble(this::trendingScore).reversed())
                .limit(limit)
                .map(this::toSummaryDto)
                .toList();
    }

    private double trendingScore(Startup s) {
        long followers = followRepository.countByStartupId(s.getId());
        long interested = startupInterestRepository.countByStartupId(s.getId());
        double hoursOld = java.time.Duration.between(s.getCreatedAt(), java.time.Instant.now()).toMinutes() / 60.0;
        double recencyBonus = Math.max(0, 336 - hoursOld) / 10.0;
        return s.getViewCount() * 1.0 + followers * 3.0 + interested * 5.0 + recencyBonus;
    }

    /** Increments the view counter unless the viewer is the startup's own founder. */
    @Transactional
    public void recordView(User viewer, UUID startupId) {
        Startup startup = findOrThrow(startupId);
        if (viewer == null || !startup.getFounder().getId().equals(viewer.getId())) {
            startupRepository.incrementViewCount(startupId);
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<StartupSummaryDto> naturalLanguageSearch(String query, Pageable pageable) {
        StartupSearchFilters filters = nlSearchService.parse(query);
        Page<Startup> page = startupRepository.searchAdvanced(
                filters.keyword(), filters.stage(), filters.minFunding(), filters.maxFunding(), pageable);
        return PageResponse.from(page.map(this::toSummaryDto));
    }

    @Transactional(readOnly = true)
    public List<StartupSummaryDto> listMine(User founder) {
        return startupRepository.findAllByFounderIdOrderByCreatedAtDesc(founder.getId())
                .stream().map(this::toSummaryDto).toList();
    }

    @Transactional
    public StartupDto publish(User founder, UUID startupId) {
        Startup startup = getOwned(founder, startupId);
        startup.setPublished(true);
        return toDto(startupRepository.save(startup));
    }

    @Transactional
    public StartupDto unpublish(User founder, UUID startupId) {
        Startup startup = getOwned(founder, startupId);
        startup.setPublished(false);
        return toDto(startupRepository.save(startup));
    }

    @Transactional
    public void delete(User founder, UUID startupId) {
        Startup startup = getOwned(founder, startupId);
        startupRepository.delete(startup);
    }

    public Startup findOrThrow(UUID startupId) {
        return startupRepository.findById(startupId)
                .orElseThrow(() -> new ResourceNotFoundException("Startup not found"));
    }

    /** Package-visible-in-spirit: used by InvestmentService to confirm a recorded investment. */
    @Transactional
    public StartupDto increaseFundingProgress(User founder, UUID startupId, java.math.BigDecimal amount) {
        Startup startup = getOwned(founder, startupId);
        java.math.BigDecimal current = startup.getFundingProgress() != null ? startup.getFundingProgress() : java.math.BigDecimal.ZERO;
        startup.setFundingProgress(current.add(amount));
        return toDto(startupRepository.save(startup));
    }

    private Startup getOwned(User founder, UUID startupId) {
        Startup startup = findOrThrow(startupId);
        if (!startup.getFounder().getId().equals(founder.getId())) {
            throw new AccessDeniedCustomException("You do not own this startup");
        }
        return startup;
    }

    private boolean isOwnerOrAdmin(User viewer, Startup startup) {
        if (viewer == null) {
            return false;
        }
        return viewer.getRole() == Role.ADMIN || startup.getFounder().getId().equals(viewer.getId());
    }

    private StartupDto toDto(Startup s) {
        String founderName = userDisplayNameService.resolveFullName(s.getFounder());
        long interestedCount = startupInterestRepository.countByStartupId(s.getId());
        return new StartupDto(
                s.getId(), s.getFounder().getId(), founderName, s.getName(), s.getLogoUrl(), s.getCoverImageUrl(),
                s.getIndustry(), s.getCountry(), s.getStage(), s.getProblem(), s.getSolution(), s.getBusinessModel(),
                s.getRevenueModel(), s.getTargetAudience(), s.getMarket(), s.getCompetitors(),
                s.getFundingGoal(), s.getFundingProgress(), s.getPitchDeckUrl(), s.getDemoVideoUrl(),
                s.getEquityOffered(), s.getWebsiteUrl(), s.getSocialLinks(),
                s.isPublished(), s.isVerified(), interestedCount, s.getViewCount(), s.getCreatedAt(), s.getUpdatedAt()
        );
    }

    public StartupSummaryDto toSummaryDto(Startup s) {
        long interestedCount = startupInterestRepository.countByStartupId(s.getId());
        return new StartupSummaryDto(s.getId(), s.getName(), s.getLogoUrl(), s.getIndustry(), s.getStage(),
                s.getFundingGoal(), s.getFundingProgress(), s.isPublished(), s.isVerified(), interestedCount);
    }
}
