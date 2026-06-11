package com.aims.backend.dto.mainpage;

import com.aims.backend.domain.mainpage.TaskStatus;
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
    @Schema(description = "�ъ�⑹�� ���� 議고�� ���� ��蹂�")
    public static class MainPageTaskDTO {

        @Schema(description = "���� ��紐�", example = "���� 以�鍮�")
        private String taskTitle;

        @Schema(description = "���� ����", example = "TODO")
        private TaskStatus taskStatus;

        @Schema(description = "예정 시간", example = "2026-06-11T09:00:00")
        private LocalDateTime scheduledAt;
    }
}
