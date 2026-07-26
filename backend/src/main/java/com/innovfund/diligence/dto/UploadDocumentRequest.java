package com.innovfund.diligence.dto;

import com.innovfund.diligence.entity.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UploadDocumentRequest(
        @NotBlank String title,
        @NotBlank String url,
        @NotNull DocumentType documentType
) {
}
