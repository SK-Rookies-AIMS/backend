package com.aims.backend.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManufacturingEventRequest {

    private String eventId;

    private Long carMasterId;

    private String processCode;

    private String eventTime;

    private Integer processingTimeSec;

    private Integer waitingTimeSec;

    private Integer stationDelaySec;
}