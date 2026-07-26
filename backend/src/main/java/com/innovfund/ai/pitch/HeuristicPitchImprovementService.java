package com.innovfund.ai.pitch;

import com.innovfund.startup.entity.Startup;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Rewriting narrative text into a genuinely stronger, investor-friendly version requires a real
 * language model — a rule-based heuristic can critique text (see {@link PitchReviewService}) but
 * can't safely fabricate improved content without risking inaccurate claims. This fallback is
 * honest about that limit: it returns the original text plus a generic, actionable checklist,
 * rather than pretending to have rewritten it. See {@link GeminiPitchImprovementService} for the
 * real LLM-backed rewrite used once Gemini is configured.
 */
@Service
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "mock", matchIfMissing = true)
public class HeuristicPitchImprovementService implements PitchImprovementService {

    private static final String MODEL_VERSION = "mock-heuristic-v1";

    @Override
    public PitchImprovementResult improve(Startup startup) {
        return new PitchImprovementResult(
                passthrough(startup.getProblem(), "problem statement"),
                passthrough(startup.getSolution(), "solution"),
                passthrough(startup.getBusinessModel(), "business model"),
                List.of(
                        "Lead with the customer, not the product: \"For [audience] who [pain], we [do X]\" reads stronger than \"We are a platform that...\"",
                        "Replace vague claims (\"huge market\", \"many customers\") with specific numbers wherever you have them.",
                        "Cut filler adjectives (\"innovative\", \"revolutionary\") — let the specifics make the case instead.",
                        "State the ask explicitly: how much funding, for what, and what it unlocks."
                ),
                MODEL_VERSION
        );
    }

    private String passthrough(String text, String fieldLabel) {
        if (text == null || text.isBlank()) {
            return "No " + fieldLabel + " provided yet — write 2-3 concrete sentences, then request an AI rewrite once Gemini is configured for a genuinely improved version.";
        }
        return text.trim() + "\n\n[This is your original text, unchanged — rewriting content requires a real language model. Configure Gemini for an actual investor-ready rewrite.]";
    }
}
