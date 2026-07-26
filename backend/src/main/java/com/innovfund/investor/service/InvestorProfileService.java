package com.innovfund.investor.service;

import com.innovfund.common.ResourceNotFoundException;
import com.innovfund.investor.dto.FeaturedInvestorDto;
import com.innovfund.investor.dto.InvestorProfileDto;
import com.innovfund.investor.dto.UpdateInvestorProfileRequest;
import com.innovfund.investor.entity.InvestorProfile;
import com.innovfund.investor.repository.InvestorProfileRepository;
import com.innovfund.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvestorProfileService {

    private final InvestorProfileRepository investorProfileRepository;

    @Transactional(readOnly = true)
    public InvestorProfileDto getByUser(User user) {
        return toDto(findByUserId(user.getId()));
    }

    @Transactional(readOnly = true)
    public InvestorProfileDto getById(UUID id) {
        InvestorProfile profile = investorProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Investor profile not found"));
        return toDto(profile);
    }

    /** Public landing-page showcase — verified investors only, limited fields. */
    @Transactional(readOnly = true)
    public List<FeaturedInvestorDto> featured(int limit) {
        return investorProfileRepository.findAllByVerified(true).stream()
                .limit(limit)
                .map(p -> new FeaturedInvestorDto(p.getUser().getId(), p.getFullName(), p.getFirmName(), p.getInvestmentInterests()))
                .toList();
    }

    @Transactional
    public InvestorProfileDto update(User user, UpdateInvestorProfileRequest request) {
        InvestorProfile profile = findByUserId(user.getId());
        profile.setFullName(request.fullName());
        profile.setBio(request.bio());
        profile.setFirmName(request.firmName());
        profile.setInvestmentInterests(request.investmentInterests());
        return toDto(investorProfileRepository.save(profile));
    }

    private InvestorProfile findByUserId(UUID userId) {
        return investorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Investor profile not found"));
    }

    private InvestorProfileDto toDto(InvestorProfile profile) {
        return new InvestorProfileDto(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getEmail(),
                profile.getFullName(),
                profile.getBio(),
                profile.getFirmName(),
                profile.getInvestmentInterests(),
                profile.isVerified(),
                profile.getCreatedAt()
        );
    }
}
