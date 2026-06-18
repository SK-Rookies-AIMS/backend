package com.aims.backend.dto.dashboard;

public record AgvStatusSummaryResponse(

        long totalCount,

        long movingCount,

        long waitingCount,

        long returningCount

) {
}