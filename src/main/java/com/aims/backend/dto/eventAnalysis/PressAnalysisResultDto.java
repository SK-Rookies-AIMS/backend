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
public class PressAnalysisResultDto {

    private Long analysisResultId;

    private Integer countIncreaseYn;

    private Double targetCycleTimeSec;

    private Double actualCycleTimeSec;

    private Double cycleTimeGapSec;

    private Double timestampDelaySec;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
