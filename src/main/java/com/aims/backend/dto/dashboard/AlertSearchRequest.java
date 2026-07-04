package com.aims.backend.dto.dashboard;

import com.aims.backend.domain.alert.Severity;
import com.aims.backend.domain.dashboard.enums.ProcessCode;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
public class AlertSearchRequest {
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;
    private Severity severity;
    private String stationCode; // completed, required, not_required
    private ProcessCode processCode; // ASSEMBLY, BODY, PAINT, PRESS
    private Double priorityScore;
    private String titleOrContents;
    
    private int page = 0;
    private int size = 10; // default 10
}
