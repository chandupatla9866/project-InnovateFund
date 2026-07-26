package com.innovfund.ai.mentor;

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

/**
 * Real LLM-backed startup mentor chatbot. Unlike {@link HeuristicMentorService}, this can answer
 * genuinely open-ended founder questions (not just funding/hiring/valuation/scaling templates),
 * grounded in the specific startup's stage, industry, funding goal, and narrative fields.
 */
@Service
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "gemini")
public class GeminiMentorService implements MentorService {

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
    public MentorAnswer answer(Startup startup, String question) {
        if (apiKey.isBlank()) {
            throw new BadRequestException("Gemini API key is not configured (GEMINI_API_KEY)");
        }

        try {
            String requestJson = objectMapper.writeValueAsString(buildRequestBody(startup, question));
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

            return new MentorAnswer(text.trim(), "Startup profile (" + startup.getName() + ") + Gemini", model);
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Gemini request failed: " + e.getMessage());
        }
    }

    private ObjectNode buildRequestBody(Startup startup, String question) {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode contents = root.putArray("contents");
        ArrayNode parts = contents.addObject().putArray("parts");
        parts.addObject().put("text", buildPrompt(startup, question));
        return root;
    }

    private String buildPrompt(Startup startup, String question) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an experienced startup mentor advising a founder through InnovateFund, ")
                .append("a startup-funding platform. Answer the founder's question below directly and ")
                .append("actionably in 3-5 sentences, grounded in their specific startup's context where relevant. ")
                .append("Do not pad with generic disclaimers.\n\n")
                .append("Startup context:\n")
                .append("Name: ").append(nullToNA(startup.getName())).append('\n')
                .append("Industry: ").append(nullToNA(startup.getIndustry())).append('\n')
                .append("Stage: ").append(startup.getStage() != null ? startup.getStage().name() : "N/A").append('\n')
                .append("Funding goal: ").append(startup.getFundingGoal() != null ? startup.getFundingGoal().toPlainString() : "N/A").append('\n')
                .append("Problem: ").append(nullToNA(startup.getProblem())).append('\n')
                .append("Solution: ").append(nullToNA(startup.getSolution())).append('\n')
                .append("Target audience: ").append(nullToNA(startup.getTargetAudience())).append("\n\n")
                .append("Founder's question: ").append(question);
        return sb.toString();
    }

    private String nullToNA(String s) {
        return (s == null || s.isBlank()) ? "N/A" : s;
    }
}
