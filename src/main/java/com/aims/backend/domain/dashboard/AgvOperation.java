package com.aims.backend.domain.dashboard;

import com.aims.backend.domain.commons.BaseEntity;
import com.aims.backend.domain.dashboard.enums.AgvStatus;
import com.aims.backend.domain.dashboard.enums.ProcessCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "agv_operation")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AgvOperation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id")
    private String eventId;

    @Column(name = "car_master_id")
    private Long carMasterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "agv_status", nullable = false)
    private AgvStatus agvStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_process", nullable = false)
    private ProcessCode currentProcess;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_process", nullable = false)
    private ProcessCode targetProcess;

    @Column(name = "route_code")
    private String routeCode;

    @Column(name = "lane_no")
    private Integer laneNo;

    public void dispatch(
            String eventId,
            Long carMasterId,
            ProcessCode from,
            ProcessCode to,
            String routeCode
    ) {
        this.eventId = eventId;
        this.carMasterId = carMasterId;
        this.currentProcess = from;
        this.targetProcess = to;
        this.routeCode = routeCode;
        this.agvStatus = AgvStatus.MOVING;
    }

    public void changeToUnloading() {
        this.currentProcess = this.targetProcess;
        this.agvStatus = AgvStatus.UNLOADING;
    }

    public void changeToReturning() {
        ProcessCode arrivedProcess = this.currentProcess;
        ProcessCode homeProcess = getHomeProcessByRouteCode(this.routeCode);

        this.currentProcess = arrivedProcess;
        this.targetProcess = homeProcess;
        this.agvStatus = AgvStatus.RETURNING;
    }

    public void changeToWaiting(
            ProcessCode homeProcess,
            ProcessCode nextTarget,
            String routeCode
    ) {
        this.eventId = null;
        this.carMasterId = null;
        this.currentProcess = homeProcess;
        this.targetProcess = nextTarget;
        this.routeCode = routeCode;
        this.agvStatus = AgvStatus.WAITING;
    }

    private ProcessCode getHomeProcessByRouteCode(String routeCode) {
        if ("PRESS_BODY".equals(routeCode)) {
            return ProcessCode.PRESS;
        }
        if ("BODY_PAINT".equals(routeCode)) {
            return ProcessCode.BODY;
        }
        if ("PAINT_ASSEMBLY".equals(routeCode)) {
            return ProcessCode.PAINT;
        }
        if ("ASSEMBLY_INSPECTION".equals(routeCode)) {
            return ProcessCode.ASSEMBLY;
        }

        return this.currentProcess;
    }
}