package com.innovfund.ai.pitch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.innovfund.common.BadRequestException;
import com.innovfund.startup.entity.Startup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Real LLM-backed rewrite of the startup's problem/solution/business-model narrative into
 * investor-friendly language, using the same structured-JSON pattern as {@link
 * com.innovfund.ai.GeminiAiEvaluationService}.
 */
@Service
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "gemini")
public class GeminiPitchImprovementService implements PitchImprovementService {

    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={key}";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${GEMINI_API_KEY:}")
    private String apiKey;

    @Value("${app.ai.gemini-model:gemini-flash-latest}")
    private String model;

    private RestClient restClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(30_000);
        return RestClient.builder().requestFactory(factory).build();
    }

    @Override
    public PitchImprovementResult improve(Startup startup) {
        if (apiKey.isBlank()) {
            throw new BadRequestException("Gemini API key is not configured (GEMINI_API_KEY)");
        }

        try {
            String requestJson = objectMapper.writeValueAsString(buildRequestBody(startup));
            String responseBody = restClient().post()
                    .uri(ENDPOINT, model, apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestJson)
                    .retrieve()
                    .body(String.class);

            JsonNode response = objectMapper.readTree(responseBody);
            JsonNode candidates = response.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                String blockReason = response.path("promptFeedback").path("blockReason").asText("unknown");
                throw new BadRequestException("Gemini returned no candidates (reason: " + blockReason + ")");
            }
            String text = candidates.get(0).path("content").path("parts").get(0).path("text").asText();
            JsonNode parsed = objectMapper.readTree(text);

            List<String> tips = new ArrayList<>();
            parsed.path("languageTips").forEach(n -> tips.add(n.asText()));

            return new PitchImprovementResult(
                    parsed.path("improvedProblemStatement").asText(""),
                    parsed.path("improvedSolution").asText(""),
                    parsed.path("improvedBusinessModel").asText(""),
                    tips,
                    model
            );
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Gemini request failed: " + e.getMessage());
        }
    }

    private ObjectNode buildRequestBody(Startup startup) {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode contents = root.putArray("contents");
        ArrayNode parts = contents.addObject().putArray("parts");
        parts.addObject().put("text", buildPrompt(startup));

        ObjectNode generationConfig = root.putObject("generationConfig");
        generationConfig.put("responseMimeType", "application/json");
        generationConfig.set("responseSchema", buildResponseSchema());
        return root;
    }

    private ObjectNode buildResponseSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "OBJECT");
        ObjectNode properties = schema.putObject("properties");
        properties.putObject("improvedProblemStatement").put("type", "STRING");
        properties.putObject("improvedSolution").put("type", "STRING");
        properties.putObject("improvedBusinessModel").put("type", "STRING");
        ObjectNode tips = properties.putObject("languageTips");
        tips.put("type", "ARRAY");
        tips.putObject("items").put("type", "STRING");
        schema.putArray("required").add("improvedProblemStatement").add("improvedSolution")
                .add("improvedBusinessModel").add("languageTips");
        return schema;
    }

    private String buildPrompt(Startup startup) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an expert startup pitch coach. Rewrite the following startup's problem statement, ")
                .append("solution, and business model into sharper, more concrete, investor-friendly versions. ")
                .append("Preserve the actual facts given — do not invent numbers, features, or claims that aren't ")
                .append("implied by the original text. If a field is empty, write a short instruction for what the ")
                .append("founder should provide instead of inventing content. Keep each rewritten field to 2-4 sentences.\n\n")
                .append("Startup name: ").append(nullToNA(startup.getName())).append('\n')
                .append("Industry: ").append(nullToNA(startup.getIndustry())).append('\n')
                .append("Target audience: ").append(nullToNA(startup.getTargetAudience())).append("\n\n")
                .append("Original problem statement: ").append(nullToNA(startup.getProblem())).append('\n')
                .append("Original solution: ").append(nullToNA(startup.getSolution())).append('\n')
                .append("Original business model: ").append(nullToNA(startup.getBusinessModel())).append("\n\n")
                .append("Also provide 3-5 general language tips for making this specific pitch sound more investor-ready.");
        return sb.toString();
    }

    private String nullToNA(String s) {
        return (s == null || s.isBlank()) ? "N/A" : s;
    }
}
