package com.aims.backend.domain.dashboard;

import com.aims.backend.domain.commons.BaseEntity;
import com.aims.backend.domain.dashboard.enums.OperationStatus;
import com.aims.backend.domain.dashboard.enums.ProcessCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "equipment_status_history")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EquipmentStatusHistory extends BaseEntity {

    @Id
    private String id;

    @Column(name = "event_id")
    private String eventId;

    @Column(name = "equipment_id")
    private Long equipmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "process_code")
    private ProcessCode processCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_status")
    private OperationStatus operationStatus;

    @Column(name = "status_change_time")
    private LocalDateTime statusChangeTime;
}
