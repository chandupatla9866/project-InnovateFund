package com.innovfund.startup.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.innovfund.startup.entity.StartupStage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

/**
 * Uses Gemini to turn a free-text query (e.g. "early-stage fintech startups looking for under $2m")
 * into structured filters. Falls back to {@link HeuristicNlSearchService} semantics on any failure
 * so a flaky AI call never breaks the search page — it just degrades to an unfiltered keyword search.
 */
@Service
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "gemini")
public class GeminiNlSearchService implements NlSearchService {

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
        factory.setReadTimeout(20_000);
        return RestClient.builder().requestFactory(factory).build();
    }

    @Override
    public StartupSearchFilters parse(String query) {
        if (apiKey.isBlank() || query == null || query.isBlank()) {
            return new StartupSearchFilters(query, null, null, null);
        }
        try {
            String requestJson = objectMapper.writeValueAsString(buildRequestBody(query));
            String responseBody = restClient().post()
                    .uri(ENDPOINT, model, apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestJson)
                    .retrieve()
                    .body(String.class);

            JsonNode response = objectMapper.readTree(responseBody);
            String text = response.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            JsonNode parsed = objectMapper.readTree(text);

            String keyword = parsed.path("keyword").isNull() ? null : parsed.path("keyword").asText(null);
            StartupStage stage = null;
            if (!parsed.path("stage").isNull() && parsed.hasNonNull("stage")) {
                try {
                    stage = StartupStage.valueOf(parsed.path("stage").asText());
                } catch (IllegalArgumentException ignored) {
                }
            }
            BigDecimal minFunding = parsed.hasNonNull("minFunding") ? BigDecimal.valueOf(parsed.path("minFunding").asDouble()) : null;
            BigDecimal maxFunding = parsed.hasNonNull("maxFunding") ? BigDecimal.valueOf(parsed.path("maxFunding").asDouble()) : null;

            return new StartupSearchFilters(keyword, stage, minFunding, maxFunding);
        } catch (Exception e) {
            // Degrade gracefully: treat the whole query as a plain keyword search rather than failing the request.
            return new StartupSearchFilters(query, null, null, null);
        }
    }

    private ObjectNode buildRequestBody(String query) {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode contents = root.putArray("contents");
        ArrayNode parts = contents.addObject().putArray("parts");
        parts.addObject().put("text", buildPrompt(query));

        ObjectNode generationConfig = root.putObject("generationConfig");
        generationConfig.put("responseMimeType", "application/json");
        generationConfig.set("responseSchema", buildResponseSchema());
        return root;
    }

    private ObjectNode buildResponseSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "OBJECT");
        ObjectNode properties = schema.putObject("properties");
        properties.putObject("keyword").put("type", "STRING")
                .put("description", "Remaining free-text search terms (industry/product keywords), or empty string if none.");
        ObjectNode stageProp = properties.putObject("stage");
        stageProp.put("type", "STRING");
        ArrayNode stageEnum = stageProp.putArray("enum");
        for (StartupStage s : StartupStage.values()) {
            stageEnum.add(s.name());
        }
        stageProp.put("nullable", true);
        properties.putObject("minFunding").put("type", "NUMBER").put("nullable", true);
        properties.putObject("maxFunding").put("type", "NUMBER").put("nullable", true);
        schema.putArray("required").add("keyword");
        return schema;
    }

    private String buildPrompt(String query) {
        return "Extract structured search filters from this natural-language startup search query for an " +
                "investor-discovery platform. Return ONLY the structured JSON.\n\n" +
                "Query: \"" + query + "\"\n\n" +
                "Rules:\n" +
                "- stage: one of IDEA, MVP, EARLY_TRACTION, GROWTH, SCALING if the query mentions a startup stage, else null.\n" +
                "- minFunding / maxFunding: numeric funding-goal thresholds in plain numbers (e.g. \"under $2m\" -> maxFunding=2000000), else null.\n" +
                "- keyword: whatever industry, product, or topic terms remain after removing stage/funding phrases " +
                "(e.g. \"fintech\", \"healthtech for elderly care\"). Empty string if nothing remains.";
    }
}
