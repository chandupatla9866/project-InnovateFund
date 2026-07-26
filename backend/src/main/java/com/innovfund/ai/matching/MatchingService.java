package com.innovfund.ai.matching;

import com.innovfund.investor.entity.InvestorProfile;
import com.innovfund.investor.repository.InvestorProfileRepository;
import com.innovfund.startup.entity.Startup;
import com.innovfund.startup.entity.StartupStage;
import com.innovfund.startup.repository.StartupRepository;
import com.innovfund.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Hybrid explainable matching: structured checks (industry substring, stage-keyword match) plus a
 * TF-IDF + cosine-similarity score between the startup's narrative fields and the investor's
 * free-text investment interests. TF-IDF is computed per-request across the current candidate set
 * (all investors when matching for a startup, all published startups when matching for an
 * investor), so term weights reflect what's actually distinctive within that comparison — not a
 * fixed global vocabulary. This is a classic IR technique, not an embeddings/semantic model; every
 * score is still broken into weighted, human-readable reasons rather than an opaque percentage.
 */
@Service
@RequiredArgsConstructor
public class MatchingService {

    private static final Pattern WORD = Pattern.compile("[a-zA-Z]{3,}");
    private static final Set<String> STOPWORDS = Set.of(
            "the", "and", "for", "with", "that", "this", "from", "your", "our", "are", "will",
            "have", "has", "into", "their", "you", "who", "can", "not", "but");

    private static final Map<StartupStage, List<String>> STAGE_KEYWORDS = Map.of(
            StartupStage.IDEA, List.of("idea", "pre-seed", "preseed"),
            StartupStage.MVP, List.of("mvp", "pre-seed", "preseed", "seed"),
            StartupStage.EARLY_TRACTION, List.of("seed", "early stage", "early-stage", "early traction"),
            StartupStage.GROWTH, List.of("series a", "growth"),
            StartupStage.SCALING, List.of("series b", "series c", "scaling", "growth")
    );

    private record Score(double total, List<String> reasons) {
    }

    private final InvestorProfileRepository investorProfileRepository;
    private final StartupRepository startupRepository;

    /** Founder's view: which investors best match this startup. */
    public List<MatchDto> matchInvestorsForStartup(Startup startup) {
        return allInvestorMatches(startup).stream().limit(10).toList();
    }

    /** This specific investor's match percent for the startup, or null if they have no profile. */
    public Double matchPercentFor(Startup startup, UUID investorUserId) {
        return allInvestorMatches(startup).stream()
                .filter(m -> m.id().equals(investorUserId))
                .map(MatchDto::matchPercent)
                .findFirst()
                .orElse(null);
    }

    private List<MatchDto> allInvestorMatches(Startup startup) {
        List<InvestorProfile> investors = investorProfileRepository.findAll();

        List<List<String>> corpus = new ArrayList<>();
        List<String> startupTerms = tokenize(startupText(startup));
        corpus.add(startupTerms);
        for (InvestorProfile investor : investors) {
            corpus.add(tokenize(nullToEmpty(investor.getInvestmentInterests())));
        }
        Map<String, Double> idf = computeIdf(corpus);
        Map<String, Double> startupVector = tfIdfVector(startupTerms, idf);

        List<MatchDto> matches = new ArrayList<>();
        for (InvestorProfile investor : investors) {
            Map<String, Double> investorVector = tfIdfVector(tokenize(nullToEmpty(investor.getInvestmentInterests())), idf);
            double cosine = cosineSimilarity(startupVector, investorVector);
            Score score = computeScore(startup, investor, cosine);
            matches.add(new MatchDto(investor.getUser().getId(), investor.getFullName(),
                    investor.getFirmName() != null ? investor.getFirmName() : "Investor",
                    score.total(), score.reasons()));
        }
        matches.sort(Comparator.comparingDouble(MatchDto::matchPercent).reversed());
        return matches;
    }

    /** Investor's view: which published startups best match their stated interests. */
    public List<MatchDto> matchStartupsForInvestor(User investorUser, InvestorProfile investor) {
        List<Startup> startups = startupRepository.findAllPublished()
                .stream().filter(s -> !s.getFounder().getId().equals(investorUser.getId())).toList();

        List<List<String>> corpus = new ArrayList<>();
        List<String> investorTerms = tokenize(nullToEmpty(investor.getInvestmentInterests()));
        corpus.add(investorTerms);
        for (Startup startup : startups) {
            corpus.add(tokenize(startupText(startup)));
        }
        Map<String, Double> idf = computeIdf(corpus);
        Map<String, Double> investorVector = tfIdfVector(investorTerms, idf);

        List<MatchDto> matches = new ArrayList<>();
        for (Startup startup : startups) {
            Map<String, Double> startupVector = tfIdfVector(tokenize(startupText(startup)), idf);
            double cosine = cosineSimilarity(startupVector, investorVector);
            Score score = computeScore(startup, investor, cosine);
            matches.add(new MatchDto(startup.getId(), startup.getName(),
                    startup.getIndustry() != null ? startup.getIndustry() : "Startup",
                    score.total(), score.reasons()));
        }
        matches.sort(Comparator.comparingDouble(MatchDto::matchPercent).reversed());
        return matches.stream().limit(10).toList();
    }

    private String startupText(Startup startup) {
        return String.join(" ", nullToEmpty(startup.getIndustry()), nullToEmpty(startup.getMarket()),
                nullToEmpty(startup.getBusinessModel()), nullToEmpty(startup.getTargetAudience()));
    }

    private Score computeScore(Startup startup, InvestorProfile investor, double cosineSimilarity) {
        List<String> reasons = new ArrayList<>();
        String interests = nullToEmpty(investor.getInvestmentInterests()).toLowerCase(Locale.ROOT);

        double industryScore = 0;
        if (startup.getIndustry() != null && interests.contains(startup.getIndustry().toLowerCase(Locale.ROOT))) {
            industryScore = 40;
            reasons.add("Same industry (" + startup.getIndustry() + ")");
        } else {
            reasons.add("Industry not explicitly mentioned in investor interests");
        }

        double stageScore = 0;
        if (startup.getStage() != null) {
            List<String> keywords = STAGE_KEYWORDS.getOrDefault(startup.getStage(), List.of());
            boolean stageMatch = keywords.stream().anyMatch(interests::contains);
            if (stageMatch) {
                stageScore = 20;
                reasons.add("Investor interests align with " + startup.getStage().name().replace('_', ' ').toLowerCase(Locale.ROOT) + " stage");
            }
        }

        double similarityScore = cosineSimilarity * 40;
        if (cosineSimilarity > 0.1) {
            reasons.add("TF-IDF cosine similarity between startup profile and investor interests (" + Math.round(cosineSimilarity * 100) + "%)");
        }

        double total = Math.round((industryScore + stageScore + similarityScore) * 10) / 10.0;
        if (reasons.isEmpty()) {
            reasons.add("Limited overlap found — investor interests may not be filled in yet");
        }

        return new Score(total, reasons);
    }

    /** Inverse document frequency (smoothed) across the given corpus of tokenized documents. */
    private Map<String, Double> computeIdf(List<List<String>> corpus) {
        Map<String, Integer> documentFrequency = new HashMap<>();
        for (List<String> doc : corpus) {
            for (String term : new HashSet<>(doc)) {
                documentFrequency.merge(term, 1, Integer::sum);
            }
        }
        int totalDocs = corpus.size();
        Map<String, Double> idf = new HashMap<>();
        for (Map.Entry<String, Integer> entry : documentFrequency.entrySet()) {
            idf.put(entry.getKey(), Math.log((double) totalDocs / (1 + entry.getValue())) + 1);
        }
        return idf;
    }

    /** Term-frequency (raw count within the doc) weighted by the corpus IDF. */
    private Map<String, Double> tfIdfVector(List<String> terms, Map<String, Double> idf) {
        Map<String, Double> termFrequency = new HashMap<>();
        for (String term : terms) {
            termFrequency.merge(term, 1.0, Double::sum);
        }
        Map<String, Double> vector = new HashMap<>();
        for (Map.Entry<String, Double> entry : termFrequency.entrySet()) {
            vector.put(entry.getKey(), entry.getValue() * idf.getOrDefault(entry.getKey(), 0.0));
        }
        return vector;
    }

    private double cosineSimilarity(Map<String, Double> a, Map<String, Double> b) {
        if (a.isEmpty() || b.isEmpty()) return 0;
        double dot = 0;
        for (Map.Entry<String, Double> entry : a.entrySet()) {
            Double bWeight = b.get(entry.getKey());
            if (bWeight != null) {
                dot += entry.getValue() * bWeight;
            }
        }
        double normA = Math.sqrt(a.values().stream().mapToDouble(v -> v * v).sum());
        double normB = Math.sqrt(b.values().stream().mapToDouble(v -> v * v).sum());
        if (normA == 0 || normB == 0) return 0;
        return dot / (normA * normB);
    }

    private List<String> tokenize(String text) {
        List<String> words = new ArrayList<>();
        var matcher = WORD.matcher(nullToEmpty(text).toLowerCase(Locale.ROOT));
        while (matcher.find()) {
            String w = matcher.group();
            if (!STOPWORDS.contains(w)) {
                words.add(w);
            }
        }
        return words;
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
