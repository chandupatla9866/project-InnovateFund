package com.innovfund.ai.pitch;

import java.util.List;

public record PitchReviewResult(List<PitchSlideFeedback> slides, String overallImpression, String modelVersion) {
}
