package com.aims.backend.repository.eventAnalysis;

import com.aims.backend.domain.eventAnalysis.BodyAnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.List;

public interface BodyAnalysisResultRepository
        extends JpaRepository<BodyAnalysisResult, Long> {

    Optional<BodyAnalysisResult> findByAnalysisResultId(Long id);

    List<BodyAnalysisResult> findAll();

        @Query("""
        SELECT b
        FROM BodyAnalysisResult b
        WHERE b.analysisResultId <> :currentId
        ORDER BY ABS(b.robotVibrationScore - :robotVibrationScore)
    """)
    List<BodyAnalysisResult> findTop20Similar(
            @Param("currentId") Long currentId,
            @Param("robotVibrationScore") Double robotVibrationScore,
            Pageable pageable
    );
}