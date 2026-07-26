package com.innovfund.ai.fraud;

import com.innovfund.startup.entity.Startup;
import com.innovfund.startup.entity.StartupStage;
import com.innovfund.startup.repository.StartupRepository;
import com.innovfund.user.service.UserDisplayNameService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * Rule-based (not ML) fraud/spam flagging for admins to triage. Deliberately simple and
 * explainable: duplicate names, copy-pasted narrative text across unrelated startups,
 * funding asks wildly out of line with the declared stage, and near-empty descriptions
 * paired with a funding ask. Every flag lists exactly which rule fired and why.
 */
@Service
@RequiredArgsConstructor
public class FraudDetectionService {

    private static final Map<StartupStage, BigDecimal> STAGE_MAX_REASONABLE_INR = Map.of(
            StartupStage.IDEA, new BigDecimal(5_000_000),
            StartupStage.MVP, new BigDecimal(10_000_000),
            StartupStage.EARLY_TRACTION, new BigDecimal(30_000_000),
            StartupStage.GROWTH, new BigDecimal(100_000_000),
            StartupStage.SCALING, new BigDecimal(500_000_000)
    );

    private final StartupRepository startupRepository;
    private final UserDisplayNameService userDisplayNameService;

    @Transactional(readOnly = true)
    public List<FraudFlagDto> detect() {
        List<Startup> all = startupRepository.findAll();
        Map<UUID, List<String>> reasonsByStartup = new LinkedHashMap<>();

        flagDuplicateNames(all, reasonsByStartup);
        flagDuplicateNarrative(all, reasonsByStartup);
        flagUnrealisticFunding(all, reasonsByStartup);
        flagLowEffortWithFundingAsk(all, reasonsByStartup);

        Map<UUID, Startup> byId = new HashMap<>();
        all.forEach(s -> byId.put(s.getId(), s));

        return reasonsByStartup.entrySet().stream()
                .map(e -> {
                    Startup s = byId.get(e.getKey());
                    String severity = e.getValue().size() >= 2 ? "HIGH" : "MEDIUM";
                    return new FraudFlagDto(s.getId(), s.getName(),
                            userDisplayNameService.resolveFullName(s.getFounder()), severity, e.getValue());
                })
                .toList();
    }

    private void flagDuplicateNames(List<Startup> all, Map<UUID, List<String>> reasons) {
        Map<String, List<Startup>> byName = new HashMap<>();
        for (Startup s : all) {
            byName.computeIfAbsent(s.getName().trim().toLowerCase(Locale.ROOT), k -> new ArrayList<>()).add(s);
        }
        for (List<Startup> group : byName.values()) {
            if (group.size() > 1) {
                for (Startup s : group) {
                    add(reasons, s.getId(), "Name \"" + s.getName() + "\" is used by " + group.size() + " different startups");
                }
            }
        }
    }

    private void flagDuplicateNarrative(List<Startup> all, Map<UUID, List<String>> reasons) {
        Map<String, List<Startup>> byProblem = new HashMap<>();
        for (Startup s : all) {
            if (s.getProblem() != null && s.getProblem().trim().length() > 30) {
                byProblem.computeIfAbsent(normalize(s.getProblem()), k -> new ArrayList<>()).add(s);
            }
        }
        for (List<Startup> group : byProblem.values()) {
            if (group.size() > 1) {
                Set<UUID> founders = new HashSet<>();
                group.forEach(s -> founders.add(s.getFounder().getId()));
                if (founders.size() > 1) {
                    for (Startup s : group) {
                        add(reasons, s.getId(), "Problem statement is identical to another startup from a different founder — possible copied pitch");
                    }
                }
            }
        }
    }

    private void flagUnrealisticFunding(List<Startup> all, Map<UUID, List<String>> reasons) {
        for (Startup s : all) {
            if (s.getFundingGoal() == null || s.getStage() == null) continue;
            BigDecimal max = STAGE_MAX_REASONABLE_INR.get(s.getStage());
            if (max != null && s.getFundingGoal().compareTo(max.multiply(BigDecimal.TEN)) > 0) {
                add(reasons, s.getId(), "Funding ask is over 10x the typical range for a "
                        + s.getStage().name().replace('_', ' ').toLowerCase(Locale.ROOT) + " stage startup");
            }
        }
    }

    private void flagLowEffortWithFundingAsk(List<Startup> all, Map<UUID, List<String>> reasons) {
        for (Startup s : all) {
            boolean tinyProblem = s.getProblem() == null || s.getProblem().trim().length() < 20;
            boolean tinySolution = s.getSolution() == null || s.getSolution().trim().length() < 20;
            boolean hasFundingAsk = s.getFundingGoal() != null && s.getFundingGoal().signum() > 0;
            if (tinyProblem && tinySolution && hasFundingAsk) {
                add(reasons, s.getId(), "Minimal problem/solution description paired with an active funding ask");
            }
        }
    }

    private String normalize(String text) {
        return text.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private void add(Map<UUID, List<String>> reasons, UUID startupId, String reason) {
        reasons.computeIfAbsent(startupId, k -> new ArrayList<>()).add(reason);
    }
}
