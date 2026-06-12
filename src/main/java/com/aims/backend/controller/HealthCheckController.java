package com.aims.backend.controller;

import com.aims.backend.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/health")
public class HealthCheckController {

    @Operation(summary = "health check API")
    @GetMapping()
    public ApiResponse<Void> processHealthCheck() {
        return ApiResponse.success("health check sucess");
    }
}
