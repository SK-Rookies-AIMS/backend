package com.aims.backend.dto.eventAnalysis;

import com.aims.backend.domain.alert.ActionCategory;
import com.aims.backend.domain.user.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionTimelineResponse {

    private Long actionId;

    private LocalDateTime actionTime;

    private String empNo;

    private String empName;

    private UserRole empRole;

    private ActionCategory actionCategory;

    private String actionContent;

    private String actionResult;
}