package com.aims.backend.dto.alert;

import com.aims.backend.domain.alert.AlertActionStatus;
import com.aims.backend.domain.alert.AlertSeverity;
import com.aims.backend.domain.alert.AlertType;
import com.aims.backend.domain.dashboard.enums.ProcessCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlertSearchRequest {

    private String from;
    private String to;
    private AlertSeverity severity;
    private AlertActionStatus actionStatus;
    private AlertType alertType;
    private ProcessCode processCode;
    private String keyword;

    private int page = 0;
    private int size = 20;
}
