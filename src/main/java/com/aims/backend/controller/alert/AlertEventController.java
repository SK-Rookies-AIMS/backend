package com.aims.backend.controller.alert;

import com.aims.backend.common.response.ApiResponse;
import com.aims.backend.config.jwt.TokenProvider;
import com.aims.backend.dto.alert.AlertActionUpdateRequest;
import com.aims.backend.dto.alert.AlertEventResponse;
import com.aims.backend.dto.alert.AlertPrioritySummaryResponse;
import com.aims.backend.dto.alert.AlertSearchRequest;
import com.aims.backend.dto.eventAnalysis.RecommendationResponse;
import com.aims.backend.service.eventAnalysis.AlertRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.aims.backend.dto.alert.ActionTimelineResponse;
import com.aims.backend.service.alert.AlertPrioritySummaryService;
import com.aims.backend.dto.alert.ActionTimelineCreateRequest;
import com.aims.backend.service.alert.AlertEventQueryService;
import com.aims.backend.service.alert.ActionTimelineService;
import com.aims.backend.service.alert.ActionTimelineService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@RestController
@RequestMapping("/api/event")
@RequiredArgsConstructor
@Validated
public class AlertEventController {

    private final AlertEventQueryService alertEventQueryService;
    private final AlertPrioritySummaryService alertPrioritySummaryService;
    private final ActionTimelineService actionTimelineService;
    private final TokenProvider tokenProvider;
    private final AlertRecommendationService alertRecommendationService;

    @GetMapping
    public ApiResponse<Page<AlertEventResponse>> getAlerts(@ModelAttribute AlertSearchRequest request) {
        return ApiResponse.success(alertEventQueryService.getAlerts(request));
    }

    @GetMapping("/priority-summary")
    public ApiResponse<AlertPrioritySummaryResponse> getPrioritySummary(
            @RequestParam(defaultValue = "7") @Min(1) @Max(365) int days
    ) {
        return ApiResponse.success(alertPrioritySummaryService.getPrioritySummary(days));
    }

    @GetMapping("/{logNo}")
    public ApiResponse<AlertEventResponse> getAlert(@PathVariable String logNo) {
        return ApiResponse.success(alertEventQueryService.getAlert(logNo));
    }

    @GetMapping("/by-event-id/{eventId}")
    public ApiResponse<AlertEventResponse> getAlertByEventId(@PathVariable String eventId) {
        return ApiResponse.success(alertEventQueryService.getAlertByEventId(eventId));
    }

    @PatchMapping("/{logNo}/action")
    public ApiResponse<AlertEventResponse> updateAction(
            @PathVariable String logNo,
            @Valid @RequestBody AlertActionUpdateRequest request
    ) {
        return ApiResponse.success(alertEventQueryService.updateAction(logNo, request));
    }

    @GetMapping("/{logNo}/action-timeline")
    public ApiResponse<List<ActionTimelineResponse>> getActionTimeline(@PathVariable String logNo) {
        return ApiResponse.success(actionTimelineService.getTimeline(logNo));
    }

    @PostMapping("/{logNo}/action-timeline")
    public ApiResponse<ActionTimelineResponse> createActionTimeline(
            @PathVariable String logNo,
            @Valid @RequestBody ActionTimelineCreateRequest request,
            HttpServletRequest httpServletRequest
    ) {


        String accessToken = TokenProvider.resolveToken(httpServletRequest);
        if (accessToken == null) {
            return ApiResponse.failure("Access token is missing or invalid.", null);
        }

        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        if (authentication == null || !(authentication.getPrincipal() instanceof TokenProvider.JwtPrincipal principal)) {
            return ApiResponse.failure("Invalid authentication principal.", null);
        }

        return ApiResponse.success(actionTimelineService.createTimeline(logNo, request, principal));
    }

    @Operation(
                summary = "유사 장애 조치 추천",
                description = "현재 이벤트와 가장 유사한 과거 이벤트를 찾아 추천 조치 방법을 반환합니다."
        )
        @GetMapping("/{logNo}/recommendation")
        public ApiResponse<RecommendationResponse> getRecommendation(
                @PathVariable String logNo) {

            RecommendationResponse response =
                    alertRecommendationService.getRecommendation(logNo);

            return ApiResponse.success(
                    response,
                    "유사 장애 추천 조회 성공"
            );
        }
}
