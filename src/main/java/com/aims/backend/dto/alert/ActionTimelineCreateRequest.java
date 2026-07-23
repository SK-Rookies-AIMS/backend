package com.aims.backend.dto.alert;

import com.aims.backend.domain.alert.ActionCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionTimelineCreateRequest {

    @NotNull(message = "조치 시간은 필수 입력 값입니다.")
    private LocalDateTime actionTime;

    @NotBlank(message = "조치 내용은 필수 입력 값입니다.")
    @Size(max = 20, message = "조치 내용은 최대 20자까지 입력 가능합니다.")
    private String actionContent;

    @NotNull(message = "조치 카테고리는 필수 입력 값입니다.")
    private ActionCategory actionCategory;

    @NotBlank(message = "조치 결과는 필수 입력 값입니다.")
    @Size(max = 20, message = "조치 결과는 최대 20자까지 입력 가능합니다.")
    private String actionResult;
}
