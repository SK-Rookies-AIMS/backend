package com.aims.backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class PasswordChangeRequest {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "비밀번호 변경 요청 정보")
    public static class PasswordChangeDTO {

        @NotBlank(message = "현재 비밀번호는 필수입니다.")
        @Schema(description = "현재 비밀번호", example = "oldPassword123")
        private String oldPassword;

        @NotBlank(message = "새 비밀번호는 필수입니다.")
        @Schema(description = "새 비밀번호", example = "newPassword123")
        private String newPassword;
    }
}
