package com.aims.backend.repository.eventAnalysis;

import com.aims.backend.domain.eventAnalysis.AssemblyAnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.List;

public interface AssemblyAnalysisResultRepository
        extends JpaRepository<AssemblyAnalysisResult, Long> {

    Optional<AssemblyAnalysisResult> findByAnalysisResultId(Long id);

    List<AssemblyAnalysisResult> findAll();

    @Query("""
        SELECT a
        FROM AssemblyAnalysisResult a
        WHERE a.analysisResultId <> :currentId
        ORDER BY ABS(a.sequenceErrorCount - :sequenceErrorCount)
    """)
    List<AssemblyAnalysisResult> findTop20Similar(
            @Param("currentId") Long currentId,
            @Param("sequenceErrorCount") Integer sequenceErrorCount,
            Pageable pageable
    );

}