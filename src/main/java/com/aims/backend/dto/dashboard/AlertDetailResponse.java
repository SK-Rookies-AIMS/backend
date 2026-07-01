package com.aims.backend.dto.dashboard;

import com.aims.backend.domain.dashboard.AlertDetail;
import com.aims.backend.domain.dashboard.enums.ProcessCode;
import com.aims.backend.domain.dashboard.enums.Severity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AlertDetailResponse {
    private String logNo;
    private Double priorityScore;
    private Severity severity;
    private ProcessCode processCode;
    private String title;
    private String contents;
    private String stationCode;
    private String actionStatus;
    private LocalDateTime createdAt;
    private String equipmentId;
    private String riskLevel;
    private Double riskScore;
    private String actionBy;

    public static AlertDetailResponse from(AlertDetail alertDetail) {
        return AlertDetailResponse.builder()
                .logNo(alertDetail.getLogNo())
                .priorityScore(alertDetail.getPriorityScore())
                .severity(alertDetail.getSeverity())
                .processCode(alertDetail.getProcessCode())
                .title(alertDetail.getTitle())
                .contents(alertDetail.getContents())
                .stationCode(alertDetail.getStationCode())
                .actionStatus(alertDetail.getActionStatus())
                .createdAt(alertDetail.getCreatedAt())
                .equipmentId(alertDetail.getEquipmentId())
                .riskLevel(alertDetail.getRiskLevel())
                .riskScore(alertDetail.getRiskScore())
                .actionBy(alertDetail.getActionBy())
                .build();
    }
}
