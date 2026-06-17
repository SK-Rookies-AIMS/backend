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

    /** AGV 출발 */
    public void dispatch(Long carMasterId, ProcessCode from, ProcessCode to, String routeCode) {
        this.carMasterId = carMasterId;
        this.currentProcess = from;
        this.targetProcess = to;
        this.routeCode = routeCode;
        this.agvStatus = AgvStatus.MOVING;
    }

    /** AGV 복귀 전환 */
    public void changeToReturning() {
        ProcessCode arrivedProcess = this.targetProcess;
        ProcessCode homeProcess = this.currentProcess;

        this.agvStatus = AgvStatus.RETURNING;
        this.currentProcess = arrivedProcess;
        this.targetProcess = homeProcess;
    }

    /** AGV 대기 전환 */
    public void changeToWaiting(ProcessCode homeProcess, ProcessCode nextProcess, String routeCode) {
        this.agvStatus = AgvStatus.WAITING;
        this.carMasterId = null;
        this.currentProcess = homeProcess;
        this.targetProcess = nextProcess;
        this.routeCode = routeCode;
    }
}