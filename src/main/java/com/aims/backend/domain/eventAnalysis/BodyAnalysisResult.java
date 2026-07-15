package com.aims.backend.domain.eventAnalysis;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "body_analysis_result")

public class BodyAnalysisResult {

    @Id
    @Column(name = "analysis_result_id")
    private Long analysisResultId;

    @Column(name = "robot_motion_status")
    private String robotMotionStatus;

    @Column(name = "robot_operation_mode")
    private String robotOperationMode;

    @Column(name = "robot_vibration_score")
    private Double robotVibrationScore;

    @Column(name = "frequency_peak_band")
    private String frequencyPeakBand;

    @Column(name = "frequency_peak_value")
    private Double frequencyPeakValue;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
