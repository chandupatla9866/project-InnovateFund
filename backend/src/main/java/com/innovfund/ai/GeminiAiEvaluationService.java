package com.innovfund.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.innovfund.common.BadRequestException;
import com.innovfund.founder.entity.FounderProfile;
import com.innovfund.founder.repository.FounderProfileRepository;
import com.innovfund.startup.entity.Startup;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Real LLM-backed evaluator using the Google Gemini API. Activated via {@code app.ai.provider=gemini}
 * (see {@link MockAiEvaluationService} for the heuristic fallback used when no key is configured).
 * Gemini is asked to return structured JSON (via responseSchema) matching the same 9-category rubric
 * used by the mock evaluator, so downstream code (AiReportService, controllers, DTOs) needs no changes.
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "gemini")
public class GeminiAiEvaluationService implements AiEvaluationService {

    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={key}";

    private final FounderProfileRepository founderProfileRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${GEMINI_API_KEY:}")
    private String apiKey;

    @Value("${app.ai.gemini-model:gemini-flash-latest}")
    private String model;

    private RestClient restClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15_000);
        factory.setReadTimeout(45_000);
        return RestClient.builder().requestFactory(factory).build();
    }

    @Override
    public AiEvaluationResult evaluate(Startup startup) {
        if (apiKey.isBlank()) {
            throw new BadRequestException("Gemini API key is not configured (GEMINI_API_KEY)");
        }

        String founderBio = founderProfileRepository.findByUserId(startup.getFounder().getId())
                .map(FounderProfile::getBio)
                .orElse("");

        String responseBody;
        try {
            String requestJson = objectMapper.writeValueAsString(buildRequestBody(startup, founderBio));
            responseBody = restClient().post()
                    .uri(ENDPOINT, model, apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestJson)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            throw new BadRequestException("Gemini request failed: " + e.getMessage());
        }

        JsonNode response;
        try {
            response = objectMapper.readTree(responseBody);
        } catch (Exception e) {
            throw new BadRequestException("Gemini returned a response that could not be parsed as JSON");
        }

        String text = extractText(response);
        JsonNode parsed;
        try {
            parsed = objectMapper.readTree(text);
        } catch (Exception e) {
            throw new BadRequestException("Gemini returned a response that could not be parsed as JSON");
        }

        Map<RubricCategory, CategoryScore> scores = new EnumMap<>(RubricCategory.class);
        for (JsonNode cat : parsed.path("categories")) {
            RubricCategory category;
            try {
                category = RubricCategory.valueOf(cat.path("category").asText(""));
            } catch (IllegalArgumentException ex) {
                continue;
            }
            double raw = Math.max(0, Math.min(20, cat.path("score").asDouble()));
            String reasoning = cat.path("reasoning").asText("");
            scores.put(category, build(category, raw, reasoning));
        }
        for (RubricCategory category : RubricCategory.values()) {
            scores.putIfAbsent(category, build(category, 0, "Gemini did not return a score for this category."));
        }

        double overallOn100 = scores.values().stream()
                .mapToDouble(cs -> (cs.rawScore() / 20.0) * cs.category().getWeight() * 100.0)
                .sum();

        List<String> strengths = new ArrayList<>();
        parsed.path("strengths").forEach(n -> strengths.add(n.asText()));
        if (strengths.isEmpty()) {
            strengths.add("No standout categories yet — every section has room to grow before this reads as a clear strength.");
        }

        List<String> suggestions = new ArrayList<>();
        parsed.path("suggestions").forEach(n -> suggestions.add(n.asText()));
        if (suggestions.isEmpty()) {
            suggestions.add("Gemini did not return specific suggestions for this submission.");
        }

        String summary = parsed.path("summary").asText(
                "AI-generated evaluation produced by Google Gemini (" + model + ").");
        String readinessStatus = parsed.path("investorReadinessStatus").asText("Not Ready Yet");
        String readinessConfidence = parsed.path("investorReadinessConfidence").asText("Low");

        return new AiEvaluationResult(scores, round(overallOn100), summary, strengths, suggestions,
                readinessStatus, readinessConfidence, model);
    }

    private ObjectNode buildRequestBody(Startup s, String founderBio) {
        ObjectNode root = objectMapper.createObjectNode();

        ArrayNode contents = root.putArray("contents");
        ObjectNode content = contents.addObject();
        ArrayNode parts = content.putArray("parts");
        parts.addObject().put("text", buildPrompt(s, founderBio));

        ObjectNode generationConfig = root.putObject("generationConfig");
        generationConfig.put("responseMimeType", "application/json");
        generationConfig.set("responseSchema", buildResponseSchema());

        return root;
    }

    private ObjectNode buildResponseSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "OBJECT");

        ObjectNode properties = schema.putObject("properties");
        properties.putObject("summary").put("type", "STRING");

        ObjectNode categories = properties.putObject("categories");
        categories.put("type", "ARRAY");
        ObjectNode categoryItem = categories.putObject("items");
        categoryItem.put("type", "OBJECT");
        ObjectNode categoryProps = categoryItem.putObject("properties");
        ArrayNode categoryEnum = categoryProps.putObject("category").put("type", "STRING").putArray("enum");
        for (RubricCategory rc : RubricCategory.values()) {
            categoryEnum.add(rc.name());
        }
        categoryProps.putObject("score").put("type", "NUMBER");
        categoryProps.putObject("reasoning").put("type", "STRING");
        categoryItem.putArray("required").add("category").add("score").add("reasoning");

        ObjectNode strengths = properties.putObject("strengths");
        strengths.put("type", "ARRAY");
        strengths.putObject("items").put("type", "STRING");

        ObjectNode suggestions = properties.putObject("suggestions");
        suggestions.put("type", "ARRAY");
        suggestions.putObject("items").put("type", "STRING");

        ObjectNode readinessStatus = properties.putObject("investorReadinessStatus");
        readinessStatus.put("type", "STRING");
        readinessStatus.putArray("enum").add("Ready for Seed Investors").add("Needs Improvement Before Pitching").add("Not Ready Yet");

        ObjectNode readinessConfidence = properties.putObject("investorReadinessConfidence");
        readinessConfidence.put("type", "STRING");
        readinessConfidence.putArray("enum").add("High").add("Medium").add("Low");

        schema.putArray("required").add("summary").add("categories").add("strengths").add("suggestions")
                .add("investorReadinessStatus").add("investorReadinessConfidence");
        return schema;
    }

    private String buildPrompt(Startup s, String founderBio) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an experienced venture-capital analyst evaluating a startup pitch submitted to ")
                .append("InnovateFund, a startup-funding platform. Score the startup across exactly these 9 rubric ")
                .append("categories, each on a 0-20 scale, grounded strictly in the text provided (do not invent facts):\n\n")
                .append("- PROBLEM_CLARITY: how clearly and specifically the problem/pain point is articulated\n")
                .append("- SOLUTION_QUALITY: how well the proposed solution addresses the stated problem\n")
                .append("- MARKET_SIZE: whether the market opportunity is sized with credible figures (TAM/SAM/SOM, growth rate)\n")
                .append("- BUSINESS_MODEL: clarity of how the startup makes money\n")
                .append("- COMPETITOR_ANALYSIS: depth of competitive landscape awareness and differentiation\n")
                .append("- TEAM_STRENGTH: founder background/experience as described in their bio\n")
                .append("- FINANCIAL_PLANNING: whether the funding goal and revenue model are coherent and justified\n")
                .append("- SCALABILITY: evidence of a path to scale beyond the current stage\n")
                .append("- FUNDING_JUSTIFICATION: whether the narrative explains what the raised funds will be used for\n\n")
                .append("Startup details:\n")
                .append("Name: ").append(nullToNA(s.getName())).append('\n')
                .append("Industry: ").append(nullToNA(s.getIndustry())).append('\n')
                .append("Stage: ").append(s.getStage() != null ? s.getStage().name() : "N/A").append('\n')
                .append("Problem: ").append(nullToNA(s.getProblem())).append('\n')
                .append("Solution: ").append(nullToNA(s.getSolution())).append('\n')
                .append("Business Model: ").append(nullToNA(s.getBusinessModel())).append('\n')
                .append("Revenue Model: ").append(nullToNA(s.getRevenueModel())).append('\n')
                .append("Target Audience: ").append(nullToNA(s.getTargetAudience())).append('\n')
                .append("Market: ").append(nullToNA(s.getMarket())).append('\n')
                .append("Competitors: ").append(nullToNA(s.getCompetitors())).append('\n')
                .append("Funding Goal: ").append(s.getFundingGoal() != null ? s.getFundingGoal().toPlainString() : "N/A").append('\n')
                .append("Founder Bio: ").append(nullToNA(founderBio)).append("\n\n")
                .append("Also provide: 2-4 genuine strengths (only for categories that are actually strong, grounded in the ")
                .append("text — do not pad with generic praise), an investorReadinessStatus (one of \"Ready for Seed Investors\", ")
                .append("\"Needs Improvement Before Pitching\", \"Not Ready Yet\"), and an investorReadinessConfidence ")
                .append("(\"High\", \"Medium\", or \"Low\") reflecting how confident you are in that readiness assessment ")
                .append("given how much information was provided.");
        return sb.toString();
    }

    private String extractText(JsonNode response) {
        if (response == null) {
            throw new BadRequestException("Gemini returned an empty response");
        }
        JsonNode candidates = response.path("candidates");
        if (!candidates.isArray() || candidates.isEmpty()) {
            String blockReason = response.path("promptFeedback").path("blockReason").asText("unknown");
            throw new BadRequestException("Gemini returned no candidates (reason: " + blockReason + ")");
        }
        JsonNode textNode = candidates.get(0).path("content").path("parts").get(0).path("text");
        if (textNode.isMissingNode()) {
            throw new BadRequestException("Gemini response was missing the expected content");
        }
        return textNode.asText();
    }

    private CategoryScore build(RubricCategory category, double rawScore, String reasoning) {
        double clamped = Math.max(0, Math.min(20, rawScore));
        double weighted = clamped * category.getWeight();
        return new CategoryScore(category, round(clamped), round(weighted), reasoning);
    }

    private String nullToNA(String s) {
        return (s == null || s.isBlank()) ? "N/A" : s;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
