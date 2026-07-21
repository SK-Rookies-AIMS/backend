package com.aims.backend.repository.eventAnalysis;

import com.aims.backend.domain.eventAnalysis.PaintAnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.List;

public interface PaintAnalysisResultRepository
        extends JpaRepository<PaintAnalysisResult, Long> {
    Optional<PaintAnalysisResult> findByAnalysisResultId(Long id);

    List<PaintAnalysisResult> findAll();
    
    @Query("""
        SELECT p
        FROM PaintAnalysisResult p
        WHERE p.analysisResultId <> :currentId
        AND p.visionLabel = :visionLabel
        ORDER BY ABS(p.surfaceQualityScore - :surfaceQualityScore)
    """)
    List<PaintAnalysisResult> findTop20Similar(
            @Param("currentId") Long currentId,
            @Param("visionLabel") String visionLabel,
            @Param("surfaceQualityScore") Double surfaceQualityScore,
            Pageable pageable
    );
}