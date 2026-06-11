package com.aims.backend.controller;

import com.aims.backend.common.response.ApiResponse;
import com.aims.backend.config.jwt.TokenProvider;
import com.aims.backend.dto.mainpage.UserTaskResponse;
import com.aims.backend.service.MainPageService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/main")
public class MainPageController {

    private final MainPageService mainPageService;
    private final TokenProvider tokenProvider;

    @Operation(summary = "사용자 작업 조회", description = "사용자별로 저장된 3개의 작업을 조회합니다.")
    @GetMapping("/task-user")
    public ApiResponse<List<UserTaskResponse.MainPageTaskDTO>> getUserTasks(HttpServletRequest request) {
        String accessToken = TokenProvider.resolveToken(request);
        if (accessToken == null) {
            return ApiResponse.failure("Access token is missing or invalid.", null);
        }

        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        if (authentication == null || !(authentication.getPrincipal() instanceof TokenProvider.JwtPrincipal jwtPrincipal)) {
            return ApiResponse.failure("Invalid authentication principal.", null);
        }

        Long EmpNo = jwtPrincipal.EmpNo();
        if (EmpNo == null) {
            return ApiResponse.failure("Employee number not found in token.", null);
        }

        List<UserTaskResponse.MainPageTaskDTO> userTasks = mainPageService.getUserTasks(EmpNo);
        return ApiResponse.success(userTasks, "사용자 작업 조회 성공입니다.");
    }
}
