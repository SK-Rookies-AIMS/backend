package com.aims.backend.domain.dashboard;

import com.aims.backend.domain.commons.BaseEntity;
import com.aims.backend.domain.dashboard.enums.AgvStatus;
import com.aims.backend.domain.dashboard.enums.ProcessCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AGV 실시간 운행 상태 테이블
 *
 * 메인 대시보드 공정 흐름도에서 사용되는
 * AGV 위치 및 상태 정보를 저장한다.
 *
 * Table : agv_operation
 */

@Getter
@Entity
@Table(name = "agv_operation")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AgvOperation extends BaseEntity {

    /**
     * AGV ID PK
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * AGV 에 담긴 차의 정보 FK
     */
    @Column(name = "car_master_id")
    private Long carMasterId;

    /**
     * AGV 현재 상태
     * MOVING / WAITING / RETURNING
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "agv_status")
    private AgvStatus agvStatus;

    /**
     * 현재 공정
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "current_process")
    private ProcessCode currentProcess;

    /**
     * 목적지 공정
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "target_process")
    private ProcessCode targetProcess;

    /**
     * 현재 이동 경로
     */
    @Column(name = "current_path")
    private String currentPath;

    /**
     * 이동 진행률 (%)
     */
    @Column(name = "progress_rate")
    private Double progressRate;

    /**
     * 지연 시간(초)
     */
    @Column(name = "delay_seconds")
    private Integer delaySeconds;

    /**
     * Route 코드
     * ex) PRESS_BODY
     */
    @Column(name = "route_code")
    private String routeCode;

    /**
     * 레인 번호
     */
    @Column(name = "lane_no")
    private Integer laneNo;
}