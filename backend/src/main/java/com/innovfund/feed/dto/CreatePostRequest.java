package com.innovfund.feed.dto;

import com.innovfund.feed.entity.PostType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreatePostRequest(
        UUID startupId,
        @NotNull PostType type,
        @NotBlank String text,
        String mediaUrl
) {
}
