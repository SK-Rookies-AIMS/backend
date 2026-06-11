package com.aims.backend.controller;

import com.aims.backend.common.response.ApiResponse;
import com.aims.backend.domain.user.User;
import com.aims.backend.dto.auth.LoginRequest;
import com.aims.backend.dto.auth.SignUpRequest;
import com.aims.backend.dto.auth.TokenResponse;
import com.aims.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "로그인", description = "로그인 후 Access Token과 Refresh Token을 반환합니다.")
    @PostMapping("/login")
    public ApiResponse<TokenResponse.TokenDTO> login(@Valid @RequestBody LoginRequest.LoginDTO loginDTO) {
        TokenResponse.TokenDTO tokenDTO = authService.login(loginDTO);
        return ApiResponse.success(tokenDTO, "로그인 성공입니다.");
    }

    @Operation(summary = "회원가입", description = "회원가입을 진행합니다.")
    @PostMapping("/signup")
    public ApiResponse<String> signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
        authService.signUp(signUpRequest);
        return ApiResponse.success(null, "회원가입이 완료되었습니다.");
    }
}
