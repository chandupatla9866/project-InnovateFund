package com.innovfund.investor.repository;

import com.innovfund.investor.entity.InvestorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvestorProfileRepository extends JpaRepository<InvestorProfile, UUID> {
    Optional<InvestorProfile> findByUserId(UUID userId);

    @Query("select i from InvestorProfile i where i.verified = :verified")
    List<InvestorProfile> findAllByVerified(boolean verified);
}
