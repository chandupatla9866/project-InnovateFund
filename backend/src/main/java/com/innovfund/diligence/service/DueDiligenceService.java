package com.innovfund.diligence.service;

import com.innovfund.common.AccessDeniedCustomException;
import com.innovfund.common.ResourceNotFoundException;
import com.innovfund.diligence.dto.DueDiligenceDocumentDto;
import com.innovfund.diligence.dto.DueDiligenceRequestDto;
import com.innovfund.diligence.dto.UploadDocumentRequest;
import com.innovfund.diligence.entity.DueDiligenceDocument;
import com.innovfund.diligence.entity.DueDiligenceRequest;
import com.innovfund.diligence.entity.DueDiligenceStatus;
import com.innovfund.diligence.repository.DueDiligenceDocumentRepository;
import com.innovfund.diligence.repository.DueDiligenceRequestRepository;
import com.innovfund.notification.entity.NotificationType;
import com.innovfund.notification.service.NotificationService;
import com.innovfund.startup.entity.Startup;
import com.innovfund.startup.service.StartupService;
import com.innovfund.user.entity.User;
import com.innovfund.user.service.UserDisplayNameService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DueDiligenceService {

    private final DueDiligenceRequestRepository requestRepository;
    private final DueDiligenceDocumentRepository documentRepository;
    private final StartupService startupService;
    private final NotificationService notificationService;
    private final UserDisplayNameService userDisplayNameService;

    @Transactional
    public DueDiligenceRequestDto requestAccess(User investor, UUID startupId) {
        Startup startup = startupService.findOrThrow(startupId);
        DueDiligenceRequest request = requestRepository.findByInvestorIdAndStartupId(investor.getId(), startupId)
                .orElseGet(() -> DueDiligenceRequest.builder().investor(investor).startup(startup).build());
        request.setStatus(DueDiligenceStatus.REQUESTED);
        request.setRespondedAt(null);
        request = requestRepository.save(request);

        notificationService.notify(startup.getFounder(), NotificationType.DUE_DILIGENCE_REQUESTED,
                userDisplayNameService.resolveFullName(investor) + " requested due diligence access to " + startup.getName(),
                "/startups/" + startup.getId() + "/due-diligence");

        return toDto(request);
    }

    @Transactional(readOnly = true)
    public DueDiligenceRequestDto myStatus(User investor, UUID startupId) {
        return requestRepository.findByInvestorIdAndStartupId(investor.getId(), startupId)
                .map(this::toDto)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<DueDiligenceRequestDto> listRequests(User founder, UUID startupId) {
        assertOwner(founder, startupId);
        return requestRepository.findAllByStartupIdOrderByCreatedAtDesc(startupId).stream().map(this::toDto).toList();
    }

    @Transactional
    public DueDiligenceRequestDto approve(User founder, UUID requestId) {
        return respond(founder, requestId, DueDiligenceStatus.APPROVED);
    }

    @Transactional
    public DueDiligenceRequestDto reject(User founder, UUID requestId) {
        return respond(founder, requestId, DueDiligenceStatus.REJECTED);
    }

    private DueDiligenceRequestDto respond(User founder, UUID requestId, DueDiligenceStatus status) {
        DueDiligenceRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));
        assertOwner(founder, request.getStartup().getId());
        request.setStatus(status);
        request.setRespondedAt(Instant.now());
        requestRepository.save(request);

        if (status == DueDiligenceStatus.APPROVED) {
            notificationService.notify(request.getInvestor(), NotificationType.DUE_DILIGENCE_APPROVED,
                    "Your due diligence request for " + request.getStartup().getName() + " was approved",
                    "/startups/" + request.getStartup().getId() + "/due-diligence");
        }
        return toDto(request);
    }

    @Transactional
    public DueDiligenceDocumentDto uploadDocument(User founder, UUID startupId, UploadDocumentRequest request) {
        Startup startup = assertOwner(founder, startupId);
        DueDiligenceDocument document = DueDiligenceDocument.builder()
                .startup(startup)
                .uploadedBy(founder)
                .title(request.title())
                .url(request.url())
                .documentType(request.documentType())
                .build();
        return toDto(documentRepository.save(document));
    }

    @Transactional
    public void deleteDocument(User founder, UUID startupId, UUID documentId) {
        assertOwner(founder, startupId);
        DueDiligenceDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        if (!document.getStartup().getId().equals(startupId)) {
            throw new ResourceNotFoundException("Document not found");
        }
        documentRepository.delete(document);
    }

    @Transactional(readOnly = true)
    public List<DueDiligenceDocumentDto> listDocuments(User viewer, UUID startupId) {
        Startup startup = startupService.findOrThrow(startupId);
        boolean isOwner = startup.getFounder().getId().equals(viewer.getId());
        if (!isOwner) {
            Optional<DueDiligenceRequest> request = requestRepository.findByInvestorIdAndStartupId(viewer.getId(), startupId);
            boolean approved = request.map(r -> r.getStatus() == DueDiligenceStatus.APPROVED).orElse(false);
            if (!approved) {
                throw new AccessDeniedCustomException("You need approved due diligence access to view these documents");
            }
        }
        return documentRepository.findAllByStartupIdOrderByCreatedAtDesc(startupId).stream().map(this::toDto).toList();
    }

    private Startup assertOwner(User founder, UUID startupId) {
        Startup startup = startupService.findOrThrow(startupId);
        if (!startup.getFounder().getId().equals(founder.getId())) {
            throw new AccessDeniedCustomException("You do not own this startup");
        }
        return startup;
    }

    private DueDiligenceRequestDto toDto(DueDiligenceRequest r) {
        return new DueDiligenceRequestDto(r.getId(), r.getInvestor().getId(),
                userDisplayNameService.resolveFullName(r.getInvestor()), r.getStartup().getId(),
                r.getStartup().getName(), r.getStatus(), r.getCreatedAt(), r.getRespondedAt());
    }

    private DueDiligenceDocumentDto toDto(DueDiligenceDocument d) {
        return new DueDiligenceDocumentDto(d.getId(), d.getTitle(), d.getUrl(), d.getDocumentType(), d.getCreatedAt());
    }
}
