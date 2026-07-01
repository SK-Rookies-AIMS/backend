package com.aims.backend.domain.dashboard;

import com.aims.backend.domain.commons.BaseEntity;
import com.aims.backend.domain.dashboard.enums.ProcessCode;
import com.aims.backend.domain.dashboard.enums.Severity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "alert_detail")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AlertDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_no")
    private String logNo;

    @Column(name = "priority_score")
    private Double priorityScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity")
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "process_code")
    private ProcessCode processCode;

    @Column(name = "title")
    private String title;

    @Column(name = "contents")
    private String contents;

    @Column(name = "station_code")
    private String stationCode;

    @Column(name = "action_status")
    private String actionStatus;

    @Column(name = "equipment_id")
    private String equipmentId;

    @Column(name = "risk_level")
    private String riskLevel;

    @Column(name = "risk_score")
    private Double riskScore;

    @Column(name = "action_by")
    private String actionBy;
}
