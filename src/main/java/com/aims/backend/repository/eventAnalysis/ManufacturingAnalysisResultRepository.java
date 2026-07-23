package com.aims.backend.repository.eventAnalysis;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aims.backend.domain.dashboard.enums.ProcessCode;
import com.aims.backend.domain.eventAnalysis.ManufacturingAnalysisResult;

import java.util.Optional;
import java.util.List;

public interface ManufacturingAnalysisResultRepository
        extends JpaRepository<ManufacturingAnalysisResult, Long> {

    Optional<ManufacturingAnalysisResult> findByEventId(String eventId);

    Optional<ManufacturingAnalysisResult> findById(Long id);

    List<ManufacturingAnalysisResult> findByProcessCode(ProcessCode processCode);

}