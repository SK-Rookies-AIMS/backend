package com.aims.backend.domain.dashboard;

import com.aims.backend.domain.dashboard.enums.OperationStatus;
import com.aims.backend.domain.dashboard.enums.ProcessCode;
import com.aims.backend.domain.dashboard.enums.EquipmentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "equipment")
@Getter
@Setter
@NoArgsConstructor
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "process_code")
    private ProcessCode processCode;

    @Column(name = "equipment_code", length = 50)
    private String equipmentCode;

    @Column(name = "equipment_name", length = 100)
    private String equipmentName;

    @Enumerated(EnumType.STRING)
    @Column(name = "equipment_type")
    private EquipmentType equipmentType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status")
    private OperationStatus currentStatus;

    /*@Column(name = "health_status")
    private String healthStatus;*/

    @Column(name = "last_fault_time")
    private LocalDateTime lastFaultTime;

    @Column(name = "last_recovered_time")
    private LocalDateTime lastRecoveredTime;

    @Column(name = "reason")
    private String reason;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
