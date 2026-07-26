package com.innovfund.startup.search;

import com.innovfund.startup.entity.StartupStage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rule-based fallback for turning a free-text search query into structured filters (stage keyword
 * matching + under/over funding amounts via regex + a keyword remainder for full-text search).
 * Replaced by {@link GeminiNlSearchService} when a real LLM is configured (app.ai.provider=gemini).
 */
@Service
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "mock", matchIfMissing = true)
public class HeuristicNlSearchService implements NlSearchService {

    // Longest/most specific phrases first so they're matched (and stripped) before shorter overlapping ones.
    private static final Map<String, StartupStage> STAGE_PHRASES = new LinkedHashMap<>();
    static {
        STAGE_PHRASES.put("early traction", StartupStage.EARLY_TRACTION);
        STAGE_PHRASES.put("early-stage", StartupStage.EARLY_TRACTION);
        STAGE_PHRASES.put("early stage", StartupStage.EARLY_TRACTION);
        STAGE_PHRASES.put("idea stage", StartupStage.IDEA);
        STAGE_PHRASES.put("minimum viable product", StartupStage.MVP);
        STAGE_PHRASES.put("growth stage", StartupStage.GROWTH);
        STAGE_PHRASES.put("scaling up", StartupStage.SCALING);
        STAGE_PHRASES.put("mvp", StartupStage.MVP);
        STAGE_PHRASES.put("idea", StartupStage.IDEA);
        STAGE_PHRASES.put("growth", StartupStage.GROWTH);
        STAGE_PHRASES.put("scaling", StartupStage.SCALING);
        STAGE_PHRASES.put("scale", StartupStage.SCALING);
    }

    private static final Pattern MAX_FUNDING = Pattern.compile(
            "(?:under|below|less than|up to)\\s*\\$?\\s*([\\d,.]+)\\s*(k|m|thousand|million|lakh|crore)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern MIN_FUNDING = Pattern.compile(
            "(?:over|above|more than|at least)\\s*\\$?\\s*([\\d,.]+)\\s*(k|m|thousand|million|lakh|crore)?", Pattern.CASE_INSENSITIVE);

    @Override
    public StartupSearchFilters parse(String query) {
        String remaining = query == null ? "" : query.toLowerCase(Locale.ROOT);

        StartupStage stage = null;
        for (Map.Entry<String, StartupStage> entry : STAGE_PHRASES.entrySet()) {
            if (remaining.contains(entry.getKey())) {
                stage = entry.getValue();
                remaining = remaining.replace(entry.getKey(), " ");
                break;
            }
        }

        BigDecimal maxFunding = null;
        Matcher maxMatcher = MAX_FUNDING.matcher(remaining);
        if (maxMatcher.find()) {
            maxFunding = parseAmount(maxMatcher.group(1), maxMatcher.group(2));
            remaining = remaining.substring(0, maxMatcher.start()) + " " + remaining.substring(maxMatcher.end());
        }

        BigDecimal minFunding = null;
        Matcher minMatcher = MIN_FUNDING.matcher(remaining);
        if (minMatcher.find()) {
            minFunding = parseAmount(minMatcher.group(1), minMatcher.group(2));
            remaining = remaining.substring(0, minMatcher.start()) + " " + remaining.substring(minMatcher.end());
        }

        String keyword = remaining
                .replaceAll("\\b(startups?|companies?|looking for|that are|with|for|and|the|a|an)\\b", " ")
                .replaceAll("\\s+", " ")
                .trim();

        return new StartupSearchFilters(keyword.isBlank() ? null : keyword, stage, minFunding, maxFunding);
    }

    private BigDecimal parseAmount(String number, String suffix) {
        BigDecimal base = new BigDecimal(number.replace(",", ""));
        if (suffix == null) {
            return base;
        }
        return switch (suffix.toLowerCase(Locale.ROOT)) {
            case "k", "thousand" -> base.multiply(BigDecimal.valueOf(1_000));
            case "m", "million" -> base.multiply(BigDecimal.valueOf(1_000_000));
            case "lakh" -> base.multiply(BigDecimal.valueOf(100_000));
            case "crore" -> base.multiply(BigDecimal.valueOf(10_000_000));
            default -> base;
        };
    }
}
