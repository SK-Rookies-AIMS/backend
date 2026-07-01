package com.aims.backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class RefreshRequest {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "토큰 갱신 요청 정보")
    public static class RefreshDTO {

        @NotBlank(message = "Refresh token은 필수입니다.")
        @Schema(description = "Refresh Token")
        private String refreshToken;
    }
}
