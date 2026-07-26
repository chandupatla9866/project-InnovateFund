package com.innovfund.feed.repository;

import com.innovfund.feed.entity.SavedStartup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SavedStartupRepository extends JpaRepository<SavedStartup, UUID> {
    Optional<SavedStartup> findByStartupIdAndUserId(UUID startupId, UUID userId);
    void deleteByStartupIdAndUserId(UUID startupId, UUID userId);
    List<SavedStartup> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
}
