package com.innovfund.ai.research;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

/**
 * Static, curated lookup table for a handful of common Indian startup categories, keyed by
 * keyword matching against the founder's free-text query. This is explicitly NOT a live market
 * data API — a real implementation would call a market-intelligence provider or an LLM with
 * web search. The data here is illustrative and should not be relied on for real decisions.
 */
@Service
public class MarketResearchService {

    private static final String MODEL_VERSION = "mock-static-lookup-v1";

    private record Category(List<String> keywords, List<String> competitors, String growth, List<String> challenges) {
    }

    private static final List<Category> CATEGORIES = List.of(
            new Category(List.of("grocery", "grocer", "organic food", "food delivery"),
                    List.of("BigBasket", "Blinkit", "Zepto", "Swiggy Instamart"),
                    "~18% CAGR (India online grocery)",
                    List.of("Last-mile logistics cost", "Customer retention", "Thin margins")),
            new Category(List.of("fintech", "payments", "lending", "neobank", "upi"),
                    List.of("Razorpay", "PhonePe", "Cred", "Groww"),
                    "~20% CAGR (India fintech)",
                    List.of("Regulatory compliance (RBI)", "Customer trust/fraud", "Customer acquisition cost")),
            new Category(List.of("edtech", "education", "learning", "upskilling"),
                    List.of("BYJU'S", "Unacademy", "PhysicsWallah", "upGrad"),
                    "~15% CAGR (India edtech, post-2022 correction)",
                    List.of("Learner retention/completion rates", "Discounting-driven sales", "Outcome proof")),
            new Category(List.of("healthtech", "health", "medicine", "telemedicine", "pharmacy"),
                    List.of("Practo", "PharmEasy", "Tata 1mg", "Cult.fit"),
                    "~19% CAGR (India digital health)",
                    List.of("Regulatory approvals", "Doctor/user trust", "Data privacy")),
            new Category(List.of("agritech", "farmer", "agriculture", "farming"),
                    List.of("DeHaat", "Ninjacart", "Agrostar", "WayCool"),
                    "~25% CAGR (India agritech)",
                    List.of("Farmer trust and adoption", "Fragmented supply chains", "Logistics in rural areas")),
            new Category(List.of("saas", "b2b software", "enterprise software"),
                    List.of("Freshworks", "Zoho", "Chargebee", "Postman"),
                    "~22% CAGR (India SaaS exports)",
                    List.of("Long enterprise sales cycles", "Global competition", "Churn management")),
            new Category(List.of("logistics", "delivery", "supply chain", "trucking"),
                    List.of("Delhivery", "Shadowfax", "Porter", "Rivigo"),
                    "~10% CAGR (India logistics)",
                    List.of("Fuel cost volatility", "Fleet utilization", "Last-mile delivery cost")),
            new Category(List.of("real estate", "proptech", "rental", "housing"),
                    List.of("NoBroker", "Housing.com", "99acres", "NestAway"),
                    "~14% CAGR (India proptech)",
                    List.of("Trust in online transactions", "Fragmented local markets", "Regulatory variance by state"))
    );

    public MarketResearchResult research(String query) {
        String q = query.toLowerCase(Locale.ROOT);
        for (Category category : CATEGORIES) {
            if (category.keywords().stream().anyMatch(q::contains)) {
                return new MarketResearchResult(query, category.competitors(), category.growth(),
                        category.challenges(),
                        "Matched against a small curated lookup table of Indian market categories — not live data.",
                        MODEL_VERSION);
            }
        }
        return new MarketResearchResult(query, List.of(),
                "Unknown — no data for this category",
                List.of("Customer acquisition", "Differentiation from incumbents", "Unit economics"),
                "No curated data matched this query. This is a generic fallback, not researched data — "
                        + "a real implementation would call a live market-intelligence API or an LLM with web search.",
                MODEL_VERSION);
    }
}
