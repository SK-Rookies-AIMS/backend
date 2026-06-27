package com.aims.backend.dto.dashboard;

import com.aims.backend.domain.dashboard.enums.ProcessCode;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonPropertyOrder({"processCode", "overallEquipmentCount", "runningEquipmentCount", "temperature", "humidity", "usage"})
public class ManufacturingStatusResponse {
    private ProcessCode processCode;
    private Long overallEquipmentCount;
    private Long runningEquipmentCount;
    private Double temperature;
    private Double humidity;
    private Integer usage;
}
