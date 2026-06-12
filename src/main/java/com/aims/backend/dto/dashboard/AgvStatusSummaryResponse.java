package com.aims.backend.dto.dashboard;

/**
 * 메인 대시보드 AGV 현황 응답 DTO
 * 운행중 / 대기중 / 복귀중
 * AGV 수를 반환한다.
 */
public record AgvStatusSummaryResponse(

        long totalCount,

        long movingCount,

        long waitingCount,

        long returningCount

) {
}