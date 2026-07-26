package com.innovfund.ai.pitch;

import com.innovfund.startup.entity.Startup;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Reviews the narrative fields already captured on a Startup as if they were pitch-deck slides
 * (Problem, Solution, Market, Business Model, Competitors, Financials). There is no PDF/slide
 * upload or parsing in Phase 1 — a real implementation would extract text per slide from an
 * uploaded deck before running the same heuristics, or hand the extracted text to an LLM.
 */
@Service
public class PitchReviewService {

    private static final String MODEL_VERSION = "mock-heuristic-v1";

    public PitchReviewResult review(Startup startup) {
        List<PitchSlideFeedback> slides = new ArrayList<>();

        slides.add(reviewTextSlide("Problem", startup.getProblem(),
                List.of("customer", "users", "pain", "struggle"),
                "Clearly name who experiences the problem and how often/severely."));

        slides.add(reviewTextSlide("Solution", startup.getSolution(),
                List.of("platform", "app", "product", "technology", "automate"),
                "Explain concretely how the product solves the stated problem, not just what it is."));

        slides.add(reviewMarketSlide(startup.getMarket()));

        slides.add(reviewTextSlide("Business Model", startup.getBusinessModel(),
                List.of("revenue", "subscription", "commission", "pricing", "fee"),
                "State exactly how money is made and at what price point."));

        slides.add(reviewCompetitorsSlide(startup.getCompetitors()));

        slides.add(reviewFinancialsSlide(startup));

        long weakCount = slides.stream().filter(s -> s.status().equals("Weak")).count();
        String overall = weakCount == 0
                ? "Strong, investor-ready narrative across all reviewed slides."
                : weakCount <= 2
                ? "Solid foundation — tighten the " + weakCount + " flagged slide(s) before sending to investors."
                : "Several slides need more detail before this deck is investor-ready.";

        return new PitchReviewResult(slides, overall, MODEL_VERSION);
    }

    private PitchSlideFeedback reviewTextSlide(String name, String text, List<String> keywords, String suggestion) {
        if (text == null || text.isBlank()) {
            return new PitchSlideFeedback(name, "Weak", "This slide is empty.", List.of("Add content — " + suggestion));
        }
        String lower = text.toLowerCase(Locale.ROOT);
        long hits = keywords.stream().filter(lower::contains).count();
        boolean detailed = text.trim().length() > 80;

        if (detailed && hits >= 1) {
            return new PitchSlideFeedback(name, "Strong", "Clear and reasonably detailed.", List.of());
        }
        if (detailed || hits >= 1) {
            return new PitchSlideFeedback(name, "Okay", "Present but could be sharper.", List.of(suggestion));
        }
        return new PitchSlideFeedback(name, "Weak", "Too brief to be convincing.", List.of(suggestion));
    }

    private PitchSlideFeedback reviewMarketSlide(String market) {
        if (market == null || market.isBlank()) {
            return new PitchSlideFeedback("Market", "Weak", "No market sizing provided.",
                    List.of("Add TAM/SAM/SOM figures", "Cite a growth rate (CAGR) if available"));
        }
        String lower = market.toLowerCase(Locale.ROOT);
        boolean hasNumbers = lower.matches(".*\\d.*");
        boolean hasFramework = lower.contains("tam") || lower.contains("sam") || lower.contains("som") || lower.contains("cagr");
        if (hasNumbers && hasFramework) {
            return new PitchSlideFeedback("Market", "Strong", "Quantified with a recognizable market-sizing framework.", List.of());
        }
        if (hasNumbers) {
            return new PitchSlideFeedback("Market", "Okay", "Has some numbers but no TAM/SAM/SOM breakdown.",
                    List.of("Break the market size into TAM, SAM, and SOM", "Use graphs where possible"));
        }
        return new PitchSlideFeedback("Market", "Weak", "No TAM/SAM/SOM, no quantified figures.",
                List.of("Add market size in currency terms", "Use graphs where possible"));
    }

    private PitchSlideFeedback reviewCompetitorsSlide(String competitors) {
        if (competitors == null || competitors.isBlank()) {
            return new PitchSlideFeedback("Competitors", "Weak", "No competitors listed.",
                    List.of("Name at least 2-3 real competitors", "Explain your differentiation"));
        }
        long count = java.util.Arrays.stream(competitors.split("[,;\\n]")).map(String::trim).filter(s -> !s.isEmpty()).count();
        if (count >= 2) {
            return new PitchSlideFeedback("Competitors", "Strong", count + " competitors listed.", List.of());
        }
        return new PitchSlideFeedback("Competitors", "Okay", "Only one competitor listed.",
                List.of("Add more named competitors for credibility"));
    }

    private PitchSlideFeedback reviewFinancialsSlide(Startup startup) {
        if (startup.getFundingGoal() == null || startup.getFundingGoal().signum() <= 0) {
            return new PitchSlideFeedback("Financials", "Weak", "No funding ask specified.",
                    List.of("State a specific funding amount and use of funds"));
        }
        boolean hasRevenueModel = startup.getRevenueModel() != null && startup.getRevenueModel().trim().length() > 40;
        if (hasRevenueModel) {
            return new PitchSlideFeedback("Financials", "Strong", "Funding ask backed by a detailed revenue model.", List.of());
        }
        return new PitchSlideFeedback("Financials", "Okay", "Funding ask present but revenue model is thin.",
                List.of("Explain unit economics and how the raise extends runway"));
    }
}
