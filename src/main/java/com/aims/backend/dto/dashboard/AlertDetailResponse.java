package com.aims.backend.dto.dashboard;

import com.aims.backend.domain.alert.AlertType;
import com.aims.backend.domain.alert.Severity;
import com.aims.backend.domain.dashboard.AlertDetail;
import com.aims.backend.domain.dashboard.enums.ProcessCode;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AlertDetailResponse {
    private String logNo;
    private String eventId;
    private AlertType alertType;  //PROGRESS, EQUIPMENT
    private ProcessCode processCode;
    private String equipmentId;
    private String eventKey;
    private Double riskScore;
    private Double occurrenceScore;
    private Double detectionScore;
    private Double priorityScore;
    private Severity severity;
    private String title;
    private String contents;
    private String actionBy;	
    private String actionStatus;		
    private String reason;
    private String scoreCalculatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public static AlertDetailResponse from(AlertDetail alertDetail) {
        return AlertDetailResponse.builder()
                .logNo(alertDetail.getLogNo())
                .eventId(alertDetail.getEventId())
                .alertType(alertDetail.getAlertType())
                .processCode(alertDetail.getProcessCode())
                .equipmentId(alertDetail.getEquipmentId())
                .eventKey(alertDetail.getEventKey())
                .riskScore(alertDetail.getRiskScore())
                .occurrenceScore(alertDetail.getOccurrenceScore())
                .detectionScore(alertDetail.getDetectionScore())
                .priorityScore(alertDetail.getPriorityScore())
                .severity(alertDetail.getSeverity())
                .title(alertDetail.getTitle())
                .contents(alertDetail.getContents())
                .actionBy(alertDetail.getActionBy())
                .actionStatus(alertDetail.getActionStatus())
                .reason(alertDetail.getReason())
                .scoreCalculatedAt(alertDetail.getScoreCalculatedAt())
                .createdAt(alertDetail.getCreatedAt())
                .resolvedAt(alertDetail.getResolvedAt())
                .build();
    }
}
