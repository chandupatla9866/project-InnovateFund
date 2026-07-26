package com.innovfund.user.service;

import com.innovfund.founder.entity.FounderProfile;
import com.innovfund.founder.repository.FounderProfileRepository;
import com.innovfund.investor.entity.InvestorProfile;
import com.innovfund.investor.repository.InvestorProfileRepository;
import com.innovfund.user.entity.Role;
import com.innovfund.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDisplayNameService {

    private final FounderProfileRepository founderProfileRepository;
    private final InvestorProfileRepository investorProfileRepository;

    public String resolveFullName(User user) {
        if (user.getRole() == Role.FOUNDER) {
            return founderProfileRepository.findByUserId(user.getId())
                    .map(FounderProfile::getFullName)
                    .orElse(user.getEmail());
        } else if (user.getRole() == Role.INVESTOR) {
            return investorProfileRepository.findByUserId(user.getId())
                    .map(InvestorProfile::getFullName)
                    .orElse(user.getEmail());
        }
        return user.getEmail();
    }
}
