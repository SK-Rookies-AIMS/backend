package com.aims.backend.dto.mainpage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class UserTaskResponse {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "메인 페이지 업무 정보")
    public static class MainPageTaskDTO {

        @Schema(description = "업무명", example = "프레스 라인 점검")
        private String taskTitle;

        @Schema(description = "업무 시간", example = "2026-06-11T09:00:00")
        private String scheduledAt;
    }
}
