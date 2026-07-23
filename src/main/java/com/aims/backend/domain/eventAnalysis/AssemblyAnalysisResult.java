package com.aims.backend.domain.eventAnalysis;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "assembly_analysis_result")

public class AssemblyAnalysisResult {
    
    @Id
    @Column(name = "analysis_result_id")
    private Long analysisResultId;

    @Column(name = "expected_sequence")
    private String expectedSequence;

    @Column(name = "actual_sequence")
    private String actualSequence;

    @Column(name = "sequence_error_count")
    private Integer sequenceErrorCount;

    @Column(name = "missing_part_count")
    private Integer missingPartCount;

    @Column(name = "fastening_error_count")
    private Integer fasteningErrorCount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
