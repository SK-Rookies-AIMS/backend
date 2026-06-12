package com.aims.backend.controller.dashboard;

import com.aims.backend.common.response.ApiResponse;
import com.aims.backend.dto.dashboard.AgvStatusSummaryResponse;
import com.aims.backend.dto.dashboard.ProcessFlowResponse;
import com.aims.backend.service.dashboard.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 메인 대시보드 API
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/main")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * AGV 상태 현황 조회
     * 운행중(MOVING), 대기중(WAITING), 복귀중(RETURNING) 상태별 AGV 수를 반환한다.
     */
    @GetMapping("/agv-status")
    public ApiResponse<AgvStatusSummaryResponse> getAgvStatusSummary() {

        return ApiResponse.success(
                dashboardService.getAgvStatusSummary()
        );
    }

    /**
     * 공정 흐름도 조회
     * 공정 간 이동 중인 AGV 목록을 조회한다.
     * 프론트는 진행률(progressRate)을 기반으로 AGV 위치를 계산하여 표시한다.
     */
    @GetMapping("/process-flow")
    public ApiResponse<ProcessFlowResponse> getProcessFlow() {

        return ApiResponse.success(
                dashboardService.getProcessFlow()
        );
    }
}