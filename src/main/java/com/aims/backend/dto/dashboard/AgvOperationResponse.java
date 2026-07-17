package com.aims.backend.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
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

    private Instant startedAt;

    private Instant expectedArrivalTime;

    private String routeCode;

    private Integer laneNo;

    private LocalDateTime updatedAt;
}