package com.aims.backend.domain.eventAnalysis;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "press_analysis_result")

public class PressAnalysisResult {
    @Id
    @Column(name = "analysis_result_id")
    private Long analysisResultId;

    @Column(name = "count_increase_yn")
    private Integer countIncreaseYn;

    @Column(name = "target_cycle_time_sec")
    private Double targetCycleTimeSec;

    @Column(name = "actual_cycle_time_sec")
    private Double actualCycleTimeSec;

    @Column(name = "cycle_time_gap_sec")
    private Double cycleTimeGapSec;

    @Column(name = "timestamp_delay_sec")
    private Double timestampDelaySec;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
