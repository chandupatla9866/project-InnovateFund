package com.innovfund.ai.mentor;

import com.innovfund.startup.entity.Startup;
import com.innovfund.startup.entity.StartupStage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

/**
 * Rule-based mentor: matches the question against a handful of common founder-question
 * categories and answers using the startup's own stage/funding/industry data, rather than a
 * generic internet answer. Used when no Gemini key is configured; see {@link GeminiMentorService}
 * for the real LLM-backed implementation capable of genuinely open-ended Q&A.
 */
@Service
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "mock", matchIfMissing = true)
public class HeuristicMentorService implements MentorService {

    private static final String MODEL_VERSION = "mock-heuristic-v1";

    private static final Map<StartupStage, BigDecimal[]> STAGE_FUNDING_RANGE_INR = Map.of(
            StartupStage.IDEA, new BigDecimal[]{new BigDecimal(1_000_000), new BigDecimal(5_000_000)},
            StartupStage.MVP, new BigDecimal[]{new BigDecimal(2_500_000), new BigDecimal(10_000_000)},
            StartupStage.EARLY_TRACTION, new BigDecimal[]{new BigDecimal(5_000_000), new BigDecimal(30_000_000)},
            StartupStage.GROWTH, new BigDecimal[]{new BigDecimal(10_000_000), new BigDecimal(100_000_000)},
            StartupStage.SCALING, new BigDecimal[]{new BigDecimal(50_000_000), new BigDecimal(500_000_000)}
    );

    public MentorAnswer answer(Startup startup, String question) {
        String q = question.toLowerCase(Locale.ROOT);

        if (containsAny(q, "fund", "raise", "crore", "lakh", "how much money", "capital")) {
            return fundingAnswer(startup);
        }
        if (containsAny(q, "valuation", "worth", "equity")) {
            return valuationAnswer(startup);
        }
        if (containsAny(q, "hire", "hiring", "team size", "headcount")) {
            return hiringAnswer(startup);
        }
        if (containsAny(q, "scale", "scaling", "grow", "expand", "expansion")) {
            return scalingAnswer(startup);
        }
        return genericAnswer(startup, question);
    }

    private MentorAnswer fundingAnswer(Startup startup) {
        StartupStage stage = startup.getStage() != null ? startup.getStage() : StartupStage.IDEA;
        BigDecimal[] range = STAGE_FUNDING_RANGE_INR.get(stage);
        StringBuilder sb = new StringBuilder();
        sb.append("For a ").append(stage.name().replace('_', ' ').toLowerCase(Locale.ROOT))
                .append(" stage startup, typical seed ranges in India run roughly ₹")
                .append(format(range[0])).append(" to ₹").append(format(range[1])).append(". ");

        if (startup.getFundingGoal() != null) {
            if (startup.getFundingGoal().compareTo(range[1]) > 0) {
                sb.append("Your current ask of ₹").append(format(startup.getFundingGoal()))
                        .append(" is above that typical range for your stage — be ready to justify it with strong traction metrics or a very large addressable market.");
            } else if (startup.getFundingGoal().compareTo(range[0]) < 0) {
                sb.append("Your current ask of ₹").append(format(startup.getFundingGoal()))
                        .append(" is on the lower end — that can be fine if your runway math supports it, but check you're not underfunding key hires.");
            } else {
                sb.append("Your current ask of ₹").append(format(startup.getFundingGoal()))
                        .append(" falls within the typical range for your stage.");
            }
        } else {
            sb.append("You haven't set a funding goal on your startup profile yet — add one so this comparison is more useful.");
        }
        return new MentorAnswer(sb.toString(), "Startup stage (" + stage + ") and funding goal", MODEL_VERSION);
    }

    private MentorAnswer valuationAnswer(Startup startup) {
        String answer = "Valuation is driven by traction, market size, team, and comparable recent deals in "
                + (startup.getIndustry() != null ? startup.getIndustry() : "your industry")
                + " — there's no single formula. As a rough heuristic, seed-stage Indian startups often price "
                + "10-20% of equity for the round. Talk to 2-3 investors before anchoring on a number, and be wary of "
                + "over-optimizing valuation over finding the right investor partner.";
        return new MentorAnswer(answer, "General seed-stage heuristics (not startup-specific data)", MODEL_VERSION);
    }

    private MentorAnswer hiringAnswer(Startup startup) {
        StartupStage stage = startup.getStage() != null ? startup.getStage() : StartupStage.IDEA;
        String answer = switch (stage) {
            case IDEA -> "At idea stage, resist hiring beyond a small founding team — validate the problem first.";
            case MVP -> "At MVP stage, prioritize one strong engineer and someone who can talk to customers daily over broad hiring.";
            case EARLY_TRACTION -> "With early traction, hire roles that remove the founders' biggest bottleneck — usually sales/growth or engineering.";
            case GROWTH -> "At growth stage, start building a leadership layer (a first VP or manager) so founders can focus on strategy.";
            case SCALING -> "At scaling stage, invest in middle management and process — headcount growth should track revenue/usage growth, not the reverse.";
        };
        return new MentorAnswer(answer, "Startup stage (" + stage + ")", MODEL_VERSION);
    }

    private MentorAnswer scalingAnswer(Startup startup) {
        String answer = "Before scaling " + startup.getName() + ", confirm you have repeatable unit economics "
                + "(customer acquisition cost < lifetime value) in your current market. "
                + (startup.getTargetAudience() != null && !startup.getTargetAudience().isBlank()
                ? "Given your target audience — " + startup.getTargetAudience() + " — expanding into an adjacent segment or city is usually safer than a completely new market."
                : "Consider expanding to an adjacent customer segment or city before a completely new market.");
        return new MentorAnswer(answer, "Startup target audience and market fields", MODEL_VERSION);
    }

    private MentorAnswer genericAnswer(Startup startup, String question) {
        String answer = "This is a heuristic response, not a full LLM-backed answer yet. Based on " + startup.getName()
                + "'s current stage (" + (startup.getStage() != null ? startup.getStage() : "not set")
                + "), focus on whichever of these is your biggest current risk: validating the problem, "
                + "reaching your first repeatable customers, or extending runway. Try rephrasing your question "
                + "around funding, hiring, valuation, or scaling for a more specific heuristic answer.";
        return new MentorAnswer(answer, "Question: \"" + question + "\" (no specific heuristic matched)", MODEL_VERSION);
    }

    private boolean containsAny(String text, String... keywords) {
        for (String k : keywords) {
            if (text.contains(k)) return true;
        }
        return false;
    }

    private String format(BigDecimal value) {
        double v = value.doubleValue();
        if (v >= 10_000_000) return String.format("%.1fCr", v / 10_000_000);
        if (v >= 100_000) return String.format("%.0fL", v / 100_000);
        return value.toPlainString();
    }
}
