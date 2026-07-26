package com.innovfund.admin.service;

import com.innovfund.common.ResourceNotFoundException;
import com.innovfund.founder.dto.FounderProfileDto;
import com.innovfund.founder.entity.FounderProfile;
import com.innovfund.founder.repository.FounderProfileRepository;
import com.innovfund.investor.dto.InvestorProfileDto;
import com.innovfund.investor.entity.InvestorProfile;
import com.innovfund.investor.repository.InvestorProfileRepository;
import com.innovfund.notification.entity.NotificationType;
import com.innovfund.notification.service.NotificationService;
import com.innovfund.startup.dto.StartupSummaryDto;
import com.innovfund.startup.entity.Startup;
import com.innovfund.startup.repository.StartupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final FounderProfileRepository founderProfileRepository;
    private final InvestorProfileRepository investorProfileRepository;
    private final StartupRepository startupRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<FounderProfileDto> listFounders(boolean verified) {
        return founderProfileRepository.findAllByVerified(verified).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<InvestorProfileDto> listInvestors(boolean verified) {
        return investorProfileRepository.findAllByVerified(verified).stream().map(this::toDto).toList();
    }

    public List<StartupSummaryDto> listStartups(boolean verified) {
        return startupRepository.findAllByVerified(verified).stream().map(this::toDto).toList();
    }

    @Transactional
    public FounderProfileDto verifyFounder(UUID id, boolean verified) {
        FounderProfile profile = founderProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Founder profile not found"));
        profile.setVerified(verified);
        FounderProfileDto dto = toDto(founderProfileRepository.save(profile));
        if (verified) {
            notificationService.notify(profile.getUser(), NotificationType.FOUNDER_VERIFIED,
                    "Your founder profile has been verified", "/founder/profile");
        }
        return dto;
    }

    @Transactional
    public InvestorProfileDto verifyInvestor(UUID id, boolean verified) {
        InvestorProfile profile = investorProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Investor profile not found"));
        profile.setVerified(verified);
        InvestorProfileDto dto = toDto(investorProfileRepository.save(profile));
        if (verified) {
            notificationService.notify(profile.getUser(), NotificationType.INVESTOR_VERIFIED,
                    "Your investor profile has been verified", "/investor/profile");
        }
        return dto;
    }

    @Transactional
    public StartupSummaryDto verifyStartup(UUID id, boolean verified) {
        Startup startup = startupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Startup not found"));
        startup.setVerified(verified);
        StartupSummaryDto dto = toDto(startupRepository.save(startup));
        if (verified) {
            notificationService.notify(startup.getFounder(), NotificationType.STARTUP_VERIFIED,
                    startup.getName() + " has been verified", "/startups/" + startup.getId());
        }
        return dto;
    }

    private FounderProfileDto toDto(FounderProfile p) {
        return new FounderProfileDto(p.getId(), p.getUser().getId(), p.getUser().getEmail(), p.getFullName(),
                p.getBio(), p.getPhone(), p.getLinkedinUrl(), p.isVerified(), p.getCreatedAt());
    }

    private InvestorProfileDto toDto(InvestorProfile p) {
        return new InvestorProfileDto(p.getId(), p.getUser().getId(), p.getUser().getEmail(), p.getFullName(),
                p.getBio(), p.getFirmName(), p.getInvestmentInterests(), p.isVerified(), p.getCreatedAt());
    }

    private StartupSummaryDto toDto(Startup s) {
        return new StartupSummaryDto(s.getId(), s.getName(), s.getLogoUrl(), s.getIndustry(), s.getStage(),
                s.getFundingGoal(), s.getFundingProgress(), s.isPublished(), s.isVerified(), 0);
    }
}
