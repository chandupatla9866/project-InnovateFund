package com.innovfund.founder.repository;

import com.innovfund.founder.entity.FounderProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FounderProfileRepository extends JpaRepository<FounderProfile, UUID> {
    Optional<FounderProfile> findByUserId(UUID userId);

    @Query("select f from FounderProfile f where f.verified = :verified")
    List<FounderProfile> findAllByVerified(boolean verified);
}
