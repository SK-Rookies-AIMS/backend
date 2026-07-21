package com.aims.backend.dto.eventAnalysis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssemblyAnalysisResultDto {

    private Long analysisResultId;

    private String expectedSequence;

    private String actualSequence;

    private Integer sequenceErrorCount;

    private Integer missingPartCount;

    private Integer fasteningErrorCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
