package com.aims.backend.dto.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ManufacturingAnalysisEvent(

        String analysisId,

        String eventId,

        Long carMasterId,

        String processCode,

        String riskLevel,

        String analysisType,

        AnalysisResult analysisResult

) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AnalysisResult(

            boolean isAbnormal,

            boolean isBottleneck,

            boolean isQualityDefect,

            boolean isEquipmentFault,

            boolean isSequenceError

    ) {
    }
}