package com.innovfund.diligence.dto;

import com.innovfund.diligence.entity.DocumentType;

import java.time.Instant;
import java.util.UUID;

public record DueDiligenceDocumentDto(
        UUID id,
        String title,
        String url,
        DocumentType documentType,
        Instant createdAt
) {
}
