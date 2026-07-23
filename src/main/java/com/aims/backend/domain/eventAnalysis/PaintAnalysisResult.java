package com.aims.backend.domain.eventAnalysis;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "paint_analysis_result")
public class PaintAnalysisResult {

    @Id
    @Column(name = "analysis_result_id")
    private Long analysisResultId;

    @Column(name = "image_position")
    private String imagePosition;

    @Column(name = "thermal_std_temp")
    private Double thermalStdTemp;

    @Column(name = "thickness_value")
    private Double thicknessValue;

    @Column(name = "defeat_score")
    private Double defeatScore;

    @Column(name = "vision_label")
    private String visionLabel;

    @Column(name = "surface_quality_score")
    private Double surfaceQualityScore;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}