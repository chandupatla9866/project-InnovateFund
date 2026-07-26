package com.innovfund.feed.repository;

import com.innovfund.feed.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface LikeRepository extends JpaRepository<Like, UUID> {
    Optional<Like> findByPostIdAndUserId(UUID postId, UUID userId);
    long countByPostId(UUID postId);
    void deleteByPostIdAndUserId(UUID postId, UUID userId);

    @Query("select count(l) from Like l where l.post.startup.id = :startupId")
    long countByPostStartupId(@Param("startupId") UUID startupId);
}
