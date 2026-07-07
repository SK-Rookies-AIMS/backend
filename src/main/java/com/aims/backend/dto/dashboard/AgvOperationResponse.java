package com.aims.backend.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AgvOperationResponse {

    private Long agvId;

    private String eventId;

    private Long carMasterId;

    private String agvStatus;

    private String currentProcess;

    private String targetProcess;

    private Double progressRate;

    private Integer delaySeconds;

    private LocalDateTime startedAt;

    private LocalDateTime expectedArrivalTime;

    private String routeCode;

    private Integer laneNo;

    private LocalDateTime updatedAt;
}