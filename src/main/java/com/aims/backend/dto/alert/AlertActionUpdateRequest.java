package com.aims.backend.dto.alert;

import com.aims.backend.domain.alert.AlertActionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AlertActionUpdateRequest {

    @NotNull
    private AlertActionStatus actionStatus;

    private String actionBy;

    private String reason;
}
