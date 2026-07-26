package com.innovfund.feed.dto;

import java.time.Instant;
import java.util.UUID;

public record CommentDto(UUID id, UUID postId, UUID authorId, String authorName, String text, Instant createdAt) {
}
