package com.aims.backend.dto.dashboard;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AlertUpdateRequest {

    @NotBlank(message = "이벤트 식별자(logNo)는 필수 입력값입니다.")
    private String logNo;

    private String actionBy;

    private String actionStatus;

    private String reason;
}
