package com.innovfund.founder.service;

import com.innovfund.common.ResourceNotFoundException;
import com.innovfund.founder.dto.FounderProfileDto;
import com.innovfund.founder.dto.UpdateFounderProfileRequest;
import com.innovfund.founder.entity.FounderProfile;
import com.innovfund.founder.repository.FounderProfileRepository;
import com.innovfund.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FounderProfileService {

    private final FounderProfileRepository founderProfileRepository;

    @Transactional(readOnly = true)
    public FounderProfileDto getByUser(User user) {
        return toDto(findByUserId(user.getId()));
    }

    @Transactional(readOnly = true)
    public FounderProfileDto getById(UUID id) {
        FounderProfile profile = founderProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Founder profile not found"));
        return toDto(profile);
    }

    @Transactional
    public FounderProfileDto update(User user, UpdateFounderProfileRequest request) {
        FounderProfile profile = findByUserId(user.getId());
        profile.setFullName(request.fullName());
        profile.setBio(request.bio());
        profile.setPhone(request.phone());
        profile.setLinkedinUrl(request.linkedinUrl());
        return toDto(founderProfileRepository.save(profile));
    }

    private FounderProfile findByUserId(UUID userId) {
        return founderProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Founder profile not found"));
    }

    private FounderProfileDto toDto(FounderProfile profile) {
        return new FounderProfileDto(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getEmail(),
                profile.getFullName(),
                profile.getBio(),
                profile.getPhone(),
                profile.getLinkedinUrl(),
                profile.isVerified(),
                profile.getCreatedAt()
        );
    }
}
