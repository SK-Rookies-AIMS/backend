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
public class BodyAnalysisResultDto {

    private Long analysisResultId;

    private String robotMotionStatus;

    private String robotOperationMode;

    private Double robotVibrationScore;

    private String frequencyPeakBand;

    private Double frequencyPeakValue;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
