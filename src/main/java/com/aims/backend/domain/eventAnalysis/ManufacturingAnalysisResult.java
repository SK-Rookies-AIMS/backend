package com.aims.backend.domain.eventAnalysis;

import com.aims.backend.domain.dashboard.enums.ProcessCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "manufacturing_analysis_result")

public class ManufacturingAnalysisResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;

    @Column(name = "car_master_id")
    private Long carMasterId;

    @Column(name = "equipment_id")
    private Long equipmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "process_code", nullable = false)
    private ProcessCode processCode;

    @Column(name = "event_time")
    private LocalDateTime eventTime;

    @Column(name = "is_abnormal")
    private Integer isAbnormal;

    @Column(name = "abnormal_type")
    private String abnormalType;

    @Column(name = "severity")
    private String severity;

    @Column(name = "risk_score")
    private Double riskScore;

    @Column(name = "analysis_message")
    private String analysisMessage;

    @Column(name = "analyzed_at")
    private LocalDateTime analyzedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
