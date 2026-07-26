package com.innovfund.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovfund.ai.AiEvaluationResult;
import com.innovfund.ai.AiEvaluationService;
import com.innovfund.ai.CategoryScore;
import com.innovfund.ai.dto.AiReportDto;
import com.innovfund.ai.dto.AiReportSummaryDto;
import com.innovfund.ai.dto.CategoryScoreDto;
import com.innovfund.ai.entity.AiReport;
import com.innovfund.ai.repository.AiReportRepository;
import com.innovfund.common.AccessDeniedCustomException;
import com.innovfund.common.ResourceNotFoundException;
import com.innovfund.startup.entity.Startup;
import com.innovfund.startup.service.StartupService;
import com.innovfund.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiReportService {

    private final AiEvaluationService aiEvaluationService;
    private final AiReportRepository aiReportRepository;
    private final StartupService startupService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public AiReportDto analyze(User founder, UUID startupId) {
        Startup startup = startupService.findOrThrow(startupId);
        if (!startup.getFounder().getId().equals(founder.getId())) {
            throw new AccessDeniedCustomException("You do not own this startup");
        }

        AiEvaluationResult result = aiEvaluationService.evaluate(startup);
        List<CategoryScoreDto> categoryDtos = result.categoryScores().values().stream()
                .map(this::toDto)
                .toList();

        AiReport report = AiReport.builder()
                .startup(startup)
                .overallScore(result.overallScore())
                .categoryScoresJson(writeJson(categoryDtos))
                .suggestionsJson(writeJson(result.suggestions()))
                .summaryText(result.summaryText())
                .strengthsJson(writeJson(result.strengths()))
                .investorReadinessStatus(result.investorReadinessStatus())
                .investorReadinessConfidence(result.investorReadinessConfidence())
                .modelVersion(result.modelVersion())
                .build();
        report = aiReportRepository.save(report);
        return toDto(report);
    }

    public List<AiReportDto> history(User viewer, UUID startupId) {
        Startup startup = startupService.findOrThrow(startupId);
        assertCanView(viewer, startup);
        return aiReportRepository.findAllByStartupIdOrderByCreatedAtDesc(startupId).stream()
                .map(this::toDto)
                .toList();
    }

    public AiReportDto latest(User viewer, UUID startupId) {
        Startup startup = startupService.findOrThrow(startupId);
        assertCanView(viewer, startup);
        AiReport report = aiReportRepository.findFirstByStartupIdOrderByCreatedAtDesc(startupId)
                .orElseThrow(() -> new ResourceNotFoundException("No AI report yet for this startup"));
        return toDto(report);
    }

    /**
     * The full report (category breakdown, strengths, suggestions) is a founder-facing coaching
     * tool, not something investors should see directly — see {@link #summary} for what investors
     * get instead. Only the owning founder or an admin can view the full report.
     */
    private void assertCanView(User viewer, Startup startup) {
        boolean isOwner = viewer != null && startup.getFounder().getId().equals(viewer.getId());
        boolean isAdmin = viewer != null && viewer.getRole() == com.innovfund.user.entity.Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedCustomException("The full AI report is only visible to the founder");
        }
    }

    /** Small investor-facing summary — overall score and readiness status only, no category breakdown. */
    public AiReportSummaryDto summary(UUID startupId) {
        Startup startup = startupService.findOrThrow(startupId);
        if (!startup.isPublished()) {
            throw new AccessDeniedCustomException("This startup is not published yet");
        }
        return aiReportRepository.findFirstByStartupIdOrderByCreatedAtDesc(startupId)
                .map(r -> new AiReportSummaryDto(r.getOverallScore(), r.getInvestorReadinessStatus(),
                        startup.getIndustry(), startup.getStage() != null ? startup.getStage().name() : null))
                .orElse(null);
    }

    private CategoryScoreDto toDto(CategoryScore cs) {
        return new CategoryScoreDto(cs.category().name(), cs.category().getDisplayName(), cs.category().getWeight(),
                cs.rawScore(), cs.weightedScore(), cs.reasoning());
    }

    private AiReportDto toDto(AiReport report) {
        return new AiReportDto(
                report.getId(),
                report.getStartup().getId(),
                report.getOverallScore(),
                readJson(report.getCategoryScoresJson(), new TypeReference<List<CategoryScoreDto>>() {
                }),
                report.getStrengthsJson() != null
                        ? readJson(report.getStrengthsJson(), new TypeReference<List<String>>() {
                        })
                        : List.of(),
                readJson(report.getSuggestionsJson(), new TypeReference<List<String>>() {
                }),
                report.getSummaryText(),
                report.getInvestorReadinessStatus(),
                report.getInvestorReadinessConfidence(),
                report.getModelVersion(),
                report.getCreatedAt()
        );
    }

    @SneakyThrows
    private String writeJson(Object value) {
        return objectMapper.writeValueAsString(value);
    }

    @SneakyThrows
    private <T> T readJson(String json, TypeReference<T> typeReference) {
        return objectMapper.readValue(json, typeReference);
    }
}
