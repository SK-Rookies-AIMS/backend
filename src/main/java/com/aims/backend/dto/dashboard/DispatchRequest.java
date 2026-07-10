package com.aims.backend.dto.dashboard;

import com.aims.backend.domain.dashboard.enums.ProcessCode;

public record DispatchRequest(
        String eventId,
        Long carMasterId,
        ProcessCode processCode
) {
}