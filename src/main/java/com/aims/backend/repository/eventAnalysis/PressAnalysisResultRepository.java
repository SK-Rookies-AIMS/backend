package com.aims.backend.repository.eventAnalysis;

import com.aims.backend.domain.eventAnalysis.PressAnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.List;


public interface PressAnalysisResultRepository
        extends JpaRepository<PressAnalysisResult, Long> {

    Optional<PressAnalysisResult> findByAnalysisResultId(Long id);

    List<PressAnalysisResult> findAll();

    @Query("""
        SELECT p
        FROM PressAnalysisResult p
        WHERE p.analysisResultId <> :currentId
        AND p.countIncreaseYn = :countIncreaseYn
        ORDER BY
            ABS(p.cycleTimeGapSec - :cycleGap),
            ABS(COALESCE(p.timestampDelaySec, 0.0) - COALESCE(:timestampDelay, 0.0))
    """)
    List<PressAnalysisResult> findMostSimilar(
            @Param("currentId") Long currentId,
            @Param("countIncreaseYn") Integer countIncreaseYn,
            @Param("cycleGap") Double cycleGap,
            @Param("timestampDelay") Double timestampDelay,
            Pageable pageable
    );
}
