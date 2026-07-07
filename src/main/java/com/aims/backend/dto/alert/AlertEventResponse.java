package com.aims.backend.dto.alert;

import com.aims.backend.domain.alert.AlertEvent;
import com.aims.backend.domain.alert.AlertType;
import com.aims.backend.domain.dashboard.enums.ProcessCode;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class AlertEventResponse {

    private String logNo;
    private String eventId;
    private AlertType alertType;
    private ProcessCode processCode;
    private Long equipmentId;
    private String eventKey;
    private BigDecimal riskScore;
    private BigDecimal occurrenceScore;
    private BigDecimal detectionScore;
    private BigDecimal priorityScore;
    private String severity;
    private String title;
    private String contents;
    private String actionBy;
    private String actionStatus;
    private String reason;
    private LocalDateTime scoreCalculatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public static AlertEventResponse from(AlertEvent alertEvent) {
        return AlertEventResponse.builder()
                .logNo(alertEvent.getLogNo())
                .eventId(alertEvent.getEventId())
                .alertType(alertEvent.getAlertType())
                .processCode(alertEvent.getProcessCode())
                .equipmentId(alertEvent.getEquipmentId())
                .eventKey(alertEvent.getEventKey())
                .riskScore(alertEvent.getRiskScore())
                .occurrenceScore(alertEvent.getOccurrenceScore())
                .detectionScore(alertEvent.getDetectionScore())
                .priorityScore(alertEvent.getPriorityScore())
                .severity(alertEvent.getSeverity() == null ? null : alertEvent.getSeverity().name())
                .title(alertEvent.getTitle())
                .contents(alertEvent.getContents())
                .actionBy(alertEvent.getActionBy())
                .actionStatus(alertEvent.getActionStatus() == null ? null : alertEvent.getActionStatus().name())
                .reason(alertEvent.getReason())
                .scoreCalculatedAt(alertEvent.getScoreCalculatedAt())
                .createdAt(alertEvent.getCreatedAt())
                .resolvedAt(alertEvent.getResolvedAt())
                .build();
    }
}
