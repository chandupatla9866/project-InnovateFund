package com.innovfund.diligence.repository;

import com.innovfund.diligence.entity.DueDiligenceDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DueDiligenceDocumentRepository extends JpaRepository<DueDiligenceDocument, UUID> {
    List<DueDiligenceDocument> findAllByStartupIdOrderByCreatedAtDesc(UUID startupId);
}
