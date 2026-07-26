package com.innovfund.ai.mentor;

import jakarta.validation.constraints.NotBlank;

public record MentorQuestionRequest(@NotBlank String question) {
}
