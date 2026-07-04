package com.aims.backend.domain.dashboard;

import com.aims.backend.domain.alert.AlertType;
import com.aims.backend.domain.alert.Severity;
import com.aims.backend.domain.dashboard.enums.ProcessCode;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

@Entity
@Table(name = "alert_event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AlertDetail{

    @Id
    @Column(name = "log_no")
    private String logNo;

    @Column(name = "event_id")
    private String eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type")
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(name = "process_code")
    private ProcessCode processCode;

    @Column(name = "equipment_id")
    private String equipmentId;

    @Column(name = "event_key")
    private String eventKey;

    @Column(name = "risk_score")
    private Double riskScore;

    @Column(name = "occurrence_score")
    private Double occurrenceScore;

    @Column(name = "detection_score")
    private Double detectionScore;

    @Column(name = "priority_score")
    private Double priorityScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity")
    private Severity severity;

    @Column(name = "title")
    private String title;

    @Column(name = "contents")
    private String contents;

    @Column(name = "action_by")
    private String actionBy;

    @Column(name = "action_status")
    private String actionStatus;

    @Column(name = "reason")
    private String reason;

    @Column(name = "score_calculated_at")
    private String scoreCalculatedAt;

    @CreatedDate
    @Column(name="created_at",nullable = false,columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public void updateAction(String actionBy, String actionStatus, String reason) {
        if (actionBy != null) {
            this.actionBy = actionBy;
        }
        
        if (actionStatus != null) {
            // 상태가 실제로 변경되었을 때만 처리하여 완료 시점(resolvedAt)의 중복 갱신이나 유실을 방지합니다.
            if (!actionStatus.equals(this.actionStatus)) {
                this.actionStatus = actionStatus;
                if (actionStatus.equalsIgnoreCase("완료") || 
                    actionStatus.equalsIgnoreCase("처리완료") || 
                    actionStatus.equalsIgnoreCase("RESOLVED") || 
                    actionStatus.equalsIgnoreCase("COMPLETED")) {
                    this.resolvedAt = LocalDateTime.now();
                } else {
                    this.resolvedAt = null;
                }
            }
        }
        
        if (reason != null) {
            this.reason = reason;
        }
    }
}