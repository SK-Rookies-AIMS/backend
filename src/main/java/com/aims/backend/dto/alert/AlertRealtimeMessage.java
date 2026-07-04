package com.aims.backend.dto.alert;

import com.aims.backend.domain.alert.AlertActionStatus;
import com.aims.backend.domain.alert.AlertSeverity;
import com.aims.backend.domain.alert.AlertType;
import com.aims.backend.domain.dashboard.enums.ProcessCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AlertRealtimeMessage(
        String eventId,
        String alertType,
        String processCode,
        String equipmentId,
        String eventKey,
        BigDecimal riskScore,
        BigDecimal occurrenceScore,
        BigDecimal detectionScore,
        BigDecimal priorityScore,
        String severity,
        String title,
        String contents,
        String actionStatus,
        LocalDateTime scoreCalculatedAt,
        LocalDateTime createdAt
) {

    public static AlertRealtimeMessage of(
            String eventId,
            AlertType alertType,
            ProcessCode processCode,
            Long equipmentId,
            String eventKey,
            BigDecimal riskScore,
            BigDecimal occurrenceScore,
            BigDecimal detectionScore,
            BigDecimal priorityScore,
            AlertSeverity severity,
            String title,
            String contents,
            AlertActionStatus actionStatus,
            LocalDateTime scoreCalculatedAt
    ) {

        return new AlertRealtimeMessage(
                eventId,
                alertType.name(),
                processCode.name(),
                equipmentId == null ? null : equipmentId.toString(),
                eventKey,
                riskScore,
                occurrenceScore,
                detectionScore,
                priorityScore,
                severity.name(),
                title,
                contents,
                actionStatus.name(),
                scoreCalculatedAt,
                scoreCalculatedAt
        );
    }
}
