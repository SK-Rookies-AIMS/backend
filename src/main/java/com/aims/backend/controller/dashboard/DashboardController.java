package com.aims.backend.controller.dashboard;

import com.aims.backend.common.response.ApiResponse;
import com.aims.backend.dto.dashboard.AgvStatusCountResponse;
import com.aims.backend.dto.dashboard.AgvStatusSummaryResponse;
import com.aims.backend.dto.dashboard.ProcessFlowResponse;
import com.aims.backend.service.dashboard.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/main")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/agv-status")
    public ApiResponse<AgvStatusSummaryResponse> getAgvStatusSummary() {

        return ApiResponse.success(
                dashboardService.getAgvStatusSummary()
        );
    }

    @GetMapping("/process-flow")
    public ApiResponse<ProcessFlowResponse> getProcessFlow() {

        return ApiResponse.success(
                dashboardService.getProcessFlow()
        );
    }
}