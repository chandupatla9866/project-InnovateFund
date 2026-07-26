package com.innovfund.ai.pitch;

import java.util.List;

public record PitchImprovementResult(
        String improvedProblemStatement,
        String improvedSolution,
        String improvedBusinessModel,
        List<String> languageTips,
        String modelVersion
) {
}
