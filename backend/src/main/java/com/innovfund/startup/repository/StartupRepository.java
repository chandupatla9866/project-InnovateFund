package com.innovfund.startup.repository;

import com.innovfund.startup.entity.Startup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface StartupRepository extends JpaRepository<Startup, UUID> {

    List<Startup> findAllByFounderIdOrderByCreatedAtDesc(UUID founderId);

    @Query("""
            select s from Startup s
            where s.published = true
            and (:industry is null or s.industry = :industry)
            and (:stage is null or s.stage = :stage)
            and (:country is null or s.country = :country)
            and (:minFunding is null or s.fundingGoal >= :minFunding)
            and (:maxFunding is null or s.fundingGoal <= :maxFunding)
            and (:minAiScore is null or exists (
                 select 1 from AiReport r where r.startup = s and r.overallScore >= :minAiScore))
            order by s.createdAt desc
            """)
    Page<Startup> searchPublished(@Param("industry") String industry,
                                   @Param("stage") com.innovfund.startup.entity.StartupStage stage,
                                   @Param("country") String country,
                                   @Param("minFunding") BigDecimal minFunding,
                                   @Param("maxFunding") BigDecimal maxFunding,
                                   @Param("minAiScore") Double minAiScore,
                                   Pageable pageable);

    @Query("select s from Startup s where s.published = true")
    List<Startup> findAllPublished();

    @Query("""
            select s from Startup s
            where s.published = true
            and (:stage is null or s.stage = :stage)
            and (:minFunding is null or s.fundingGoal >= :minFunding)
            and (:maxFunding is null or s.fundingGoal <= :maxFunding)
            and (:keyword is null or :keyword = '' or
                 lower(s.name) like lower(concat('%', :keyword, '%')) or
                 lower(s.industry) like lower(concat('%', :keyword, '%')) or
                 lower(s.problem) like lower(concat('%', :keyword, '%')) or
                 lower(s.solution) like lower(concat('%', :keyword, '%')) or
                 lower(s.market) like lower(concat('%', :keyword, '%')) or
                 lower(s.targetAudience) like lower(concat('%', :keyword, '%')))
            order by s.createdAt desc
            """)
    Page<Startup> searchAdvanced(@Param("keyword") String keyword,
                                  @Param("stage") com.innovfund.startup.entity.StartupStage stage,
                                  @Param("minFunding") BigDecimal minFunding,
                                  @Param("maxFunding") BigDecimal maxFunding,
                                  Pageable pageable);

    List<Startup> findAllByVerified(boolean verified);

    @Modifying
    @Query("update Startup s set s.viewCount = s.viewCount + 1 where s.id = :id")
    void incrementViewCount(@Param("id") UUID id);
}
