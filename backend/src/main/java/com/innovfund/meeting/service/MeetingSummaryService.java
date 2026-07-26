package com.innovfund.meeting.service;

import com.innovfund.meeting.dto.MeetingSummaryResult;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Heuristic (regex + keyword) extraction over a pasted meeting transcript — no speech-to-text,
 * no real NLP model. A real implementation would run this transcript through an LLM instead of
 * regex/keyword rules.
 */
@Service
public class MeetingSummaryService {

    private static final String MODEL_VERSION = "mock-heuristic-v1";

    private static final Pattern SENTENCE_SPLIT = Pattern.compile("(?<=[.!?])\\s+");
    private static final Pattern AMOUNT_PATTERN = Pattern.compile(
            "(?i)(₹|rs\\.?|inr|\\$)\\s?\\d+(\\.\\d+)?\\s?(lakh|lakhs|crore|crores|l|cr|k|million|thousand)?");

    private static final List<String> CONCERN_KEYWORDS = List.of(
            "concern", "worried", "risk", "competition", "not sure", "issue", "challenge", "problem with");

    private static final List<String> ACTION_KEYWORDS = List.of(
            "will send", "will share", "should", "need to", "follow up", "follow-up", "next step", "action item", "by next week", "will schedule");

    public MeetingSummaryResult summarize(String transcript) {
        String[] rawSentences = SENTENCE_SPLIT.split(transcript.trim());

        Set<String> amounts = new LinkedHashSet<>();
        Matcher amountMatcher = AMOUNT_PATTERN.matcher(transcript);
        while (amountMatcher.find()) {
            amounts.add(amountMatcher.group().trim());
        }

        List<String> concerns = filterSentences(rawSentences, CONCERN_KEYWORDS);
        List<String> actionItems = filterSentences(rawSentences, ACTION_KEYWORDS);

        String summary = String.format(
                "Heuristic summary from %d sentence(s): %s. %s. %s.",
                rawSentences.length,
                amounts.isEmpty() ? "no specific amounts mentioned" : "amounts mentioned: " + String.join(", ", amounts),
                concerns.isEmpty() ? "no explicit concerns flagged" : concerns.size() + " concern(s) flagged",
                actionItems.isEmpty() ? "no clear action items found" : actionItems.size() + " action item(s) identified"
        );

        return new MeetingSummaryResult(List.copyOf(amounts), concerns, actionItems, summary, MODEL_VERSION);
    }

    private List<String> filterSentences(String[] sentences, List<String> keywords) {
        List<String> matched = new java.util.ArrayList<>();
        for (String sentence : sentences) {
            String lower = sentence.toLowerCase(Locale.ROOT);
            if (keywords.stream().anyMatch(lower::contains)) {
                matched.add(sentence.trim());
            }
        }
        return matched;
    }
}
