package com.aims.backend.dto.dashboard;

import java.time.LocalDateTime;

public record AgvOperationResponse(
        Long id,
        Long carMasterId,
        String agvStatus,
        String currentProcess,
        String targetProcess,
        String currentPath,
        Double progressRate,
        Integer delaySeconds,
        String routeCode,
        Integer laneNo,
        LocalDateTime updatedAt
) {
}