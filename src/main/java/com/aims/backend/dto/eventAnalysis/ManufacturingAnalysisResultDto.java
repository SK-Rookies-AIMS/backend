package com.aims.backend.dto.eventAnalysis;

import com.aims.backend.domain.dashboard.enums.ProcessCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManufacturingAnalysisResultDto {

    private Long id;

    private String eventId;

    private Long carMasterId;

    private Long equipmentId;

    private ProcessCode processCode;

    private LocalDateTime eventTime;

    private Integer isAbnormal;

    private String abnormalType;

    private String severity;

    private Double riskScore;

    private String analysisMessage;

    private LocalDateTime analyzedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
