package com.aims.backend.controller.dashboard;

import com.aims.backend.common.response.ApiResponse;
import com.aims.backend.dto.dashboard.AlertDetailResponse;
import com.aims.backend.dto.dashboard.AlertSearchRequest;
import com.aims.backend.dto.dashboard.AlertUpdateRequest;
import com.aims.backend.service.dashboard.AlertDetailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/event")
@RequiredArgsConstructor
public class AlertDetailController {

    private final AlertDetailService alertDetailService;

    @GetMapping("/get_overall_events")
    public ApiResponse<Page<AlertDetailResponse>> getOverallEvents(@ModelAttribute AlertSearchRequest request) {
        return ApiResponse.success(alertDetailService.getAlerts(request));
    }   

    @PostMapping("/update_event")
    public ApiResponse<AlertDetailResponse> updateEvent(@Valid @RequestBody AlertUpdateRequest request) {
        return ApiResponse.success(alertDetailService.updateAlert(request));
    }
}
