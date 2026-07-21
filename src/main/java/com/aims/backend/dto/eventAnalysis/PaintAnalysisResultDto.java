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
public class PaintAnalysisResultDto {

    private Long analysisResultId;

    private String imagePosition;

    private Double thermalStdTemp;

    private Double thicknessValue;

    private Double defeatScore;

    private String visionLabel;

    private Double surfaceQualityScore;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
