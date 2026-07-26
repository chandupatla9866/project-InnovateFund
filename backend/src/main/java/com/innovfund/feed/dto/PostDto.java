package com.innovfund.feed.dto;

import com.innovfund.feed.entity.PostType;

import java.time.Instant;
import java.util.UUID;

public record PostDto(
        UUID id,
        UUID authorId,
        String authorName,
        UUID startupId,
        String startupName,
        String startupLogoUrl,
        PostType type,
        String text,
        String mediaUrl,
        long likeCount,
        long commentCount,
        boolean likedByMe,
        Instant createdAt
) {
}
